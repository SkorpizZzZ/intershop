package org.example.intershop.service.integration.service;

import org.example.intershop.domain.Item;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.service.ItemService;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemServiceIT extends AbstractIntegration {

    @Autowired
    private ItemService service;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.resetAllCounts()
                .zipWith(itemRepository.resetAllCartId())
                .block();
    }

    @Nested
    @DisplayName("Поиск всех итемов")
    class FindAll {

        @Test
        @DisplayName("Title не передан")
        void findAll() {
            service.findAll(PageRequest.of(0, 20), null)
                    .doOnNext(items -> assertThat(items.getTotalElements()).isEqualTo(17))
                    .block();
        }

        @Test
        @DisplayName("Title передан")
        void byTitle() {
            //GIVEN
            String expectedResult = "Галстук";
            //WHEN
            Mono<Page<ItemDto>> actualResult = service.findAll(PageRequest.of(0, 20), "Галстук");
            //THEN
            StepVerifier.create(actualResult)
                            .assertNext(itemDtos -> {
                                assertThat(itemDtos.getTotalElements()).isEqualTo(1);
                                assertThat(itemDtos.getContent().getFirst().title()).isEqualTo(expectedResult);
                            }).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Поиск итема по id")
    class FindById {
        @Test
        @DisplayName("Итем найден")
        void itemFound() {
            service.findById(1L)
                    .doOnNext(itemDto -> assertThat(itemDto.id()).isEqualTo(1L))
                    .block();
        }

        @Test
        @DisplayName("По переданному id Итем не найден")
        void itemNotFound() {
            //WHEN
            Mono<ItemDto> actualResult = service.findById(Long.MAX_VALUE);
            //THEN
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                            throwable.getMessage().equals(String.format("Item with id = %s not found", Long.MAX_VALUE)))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Убрать/добавить в корзину")
    class Action {
        @Test
        @DisplayName("Добавление итема в корзину")
        void plusAction() {
            //GIVEN
            Long expectedCount = 1L;
            //WHEN
            Mono<ItemDto> actualResult = service.action(1L, "plus");
            //THEN
            StepVerifier.create(actualResult)
                    .assertNext(result -> {
                        assertThat(result.count()).isEqualTo(expectedCount);
                        assertThat(result.cartId()).isNotNull();
                    }).verifyComplete();
        }

        @Test
        @DisplayName("Убрать итем из корзины")
        void minusAction() {
            //GIVEN
            Long expectedCount = 0L;
            Mono<Void> setup = itemRepository.findById(1L)
                    .flatMap(item -> {
                        item.setCount(1L);
                        item.setCartId(1L);
                        return itemRepository.save(item);
                    }).then();
            //THEN
            Mono<ItemDto> actualResult = setup.then(service.action(1L, "minus"));
            StepVerifier.create(actualResult)
                    .assertNext(result -> {
                        assertThat(result.count()).isEqualTo(expectedCount);
                        assertThat(result.cartId()).isNull();
                    }).verifyComplete();
        }

        @Test
        @DisplayName("Убрать итем который и так не в корзине")
        void minusActionWhenItemNotInCartYet() {
            //GIVEN
            Long expectedCount = 0L;
            //WHEN
            Mono<ItemDto> actualResult = service.action(1L, "minus");
            //THEN
            StepVerifier.create(actualResult)
                    .assertNext(result -> {
                        assertThat(result.count()).isEqualTo(expectedCount);
                        assertThat(result.cartId()).isNull();
                    }).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Поиск по cartId")
    class FindAllByCartId {
        @Test
        @DisplayName("В корзине нет ни одного итема")
        void cartIsEmpty() {
            service.findAllByCartId(1L)
                    .collectList()
                    .doOnNext(actualResult -> assertThat(actualResult).isEmpty())
                    .block();
        }

        @Test
        @DisplayName("В корзине 2 итема")
        void cartHasTwoItems() {
            //GIVEN
            Mono<Void> setup = itemRepository.findById(1L)
                    .zipWith(itemRepository.findById(2L))
                    .flatMap(tuple -> {
                        Item firstItem = tuple.getT1();
                        Item secondItem = tuple.getT2();
                        firstItem.setCartId(1L);
                        secondItem.setCartId(1L);
                        return itemRepository.saveAll(List.of(firstItem, secondItem)).then();
                    });
            //WHEN
            Mono<List<ItemDto>> actualResult = setup.then(service.findAllByCartId(1L).collectList());
            //THEN
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).hasSize(2))
                    .verifyComplete();
        }
    }
}
