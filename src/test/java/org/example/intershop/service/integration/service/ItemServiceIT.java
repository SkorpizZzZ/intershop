package org.example.intershop.service.integration.service;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.example.intershop.domain.Cart;
import org.example.intershop.domain.Item;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.service.ItemService;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ItemServiceIT extends AbstractIntegration {

    @Autowired
    private ItemService service;
    @Autowired
    private ItemRepository itemRepository;

    @Nested
    @DisplayName("Поиск всех итемов")
    class FindAll {

        @Test
        @DisplayName("Title не передан")
        void findAll() {
            Page<ItemDto> actualResult = service.findAll(PageRequest.of(0, 20), null);
            assertThat(actualResult.getTotalElements()).isEqualTo(17);
        }

        @Test
        @DisplayName("Title передан")
        void byTitle() {
            //GIVEN
            String expectedResult = "Галстук";
            //THEN
            Page<ItemDto> actualResult = service.findAll(PageRequest.of(0, 20), "Галстук");
            assertThat(actualResult.getTotalElements()).isEqualTo(1);
            assertThat(actualResult.getContent().getFirst().title()).isEqualTo(expectedResult);
        }
    }

    @Nested
    @DisplayName("Поиск итема по id")
    class FindById {
        @Test
        @DisplayName("Итем найден")
        void itemFound() {
            ItemDto foundItem = service.findById(1L);
            assertThat(foundItem.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("По переданному id Итем не найден")
        void itemNotFound() {
            Assertions.assertThatThrownBy(() -> service.findById(Long.MAX_VALUE))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(String.format("Item with id = %s not found", Long.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("Убрать/добавить в корзину")
    class Action {
        @Test
        @DisplayName("Добавление итема в корзину")
        @Transactional
        void plusAction() {
            //GIVEN
            Long expectedCount = 1L;
            //THEN
            ItemDto actualResult = service.action(1L, "plus");
            assertThat(actualResult.count()).isEqualTo(expectedCount);
            assertThat(actualResult.cartId()).isNotNull();
        }

        @Test
        @DisplayName("Убрать итем из корзины")
        @Transactional
        void minusAction() {
            //GIVEN
            Long expectedCount = 0L;
            Optional<Item> foundItem = itemRepository.findById(1L);
            foundItem.get().setCount(1L);
            foundItem.get().setCart(new Cart(1L, Collections.singletonList(foundItem.get())));
            itemRepository.save(foundItem.get());
            //THEN
            ItemDto actualResult = service.action(1L, "minus");
            assertThat(actualResult.count()).isEqualTo(expectedCount);
            assertThat(actualResult.cartId()).isNull();
        }

        @Test
        @DisplayName("Убрать итем который и так не в корзине")
        @Transactional
        void minusActionWhenItemNotInCartYet() {
            //GIVEN
            Long expectedCount = 0L;
            //THEN
            ItemDto actualResult = service.action(1L, "minus");
            assertThat(actualResult.count()).isEqualTo(expectedCount);
            assertThat(actualResult.cartId()).isNull();
        }
    }

    @Nested
    @DisplayName("Поиск по cartId")
    class FindAllByCartId {
        @Test
        @DisplayName("В корзине нет ни одного итема")
        void cartIsEmpty() {
            List<ItemDto> actualResult = service.findAllByCartId(1L);
            assertThat(actualResult).isEmpty();
        }

        @Test
        @DisplayName("В корзине 2 итема")
        @Transactional
        void cartHasTwoItems() {
            //GIVEN
            Cart cart = new Cart(1L, Collections.emptyList());
            Item firstItem = itemRepository.findById(1L).get();
            Item secondItem = itemRepository.findById(2L).get();
            firstItem.setCart(cart);
            secondItem.setCart(cart);
            cart.setItems(List.of(firstItem, secondItem));
            itemRepository.saveAll(List.of(firstItem, secondItem));
            //THEN
            List<ItemDto> actualResult = service.findAllByCartId(1L);
            assertThat(actualResult).size().isEqualTo(2L);
        }
    }
}
