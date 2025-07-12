package org.example.intershop.service.integration.service;

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
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ItemServiceIT extends AbstractIntegration {

    @Autowired
    private ItemService service;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Mono.when(
                itemRepository.resetAllCounts(),
                itemRepository.resetAllCartId()
        ).block();
        Objects.requireNonNull(cacheManager.getCache("item")).clear();
        Objects.requireNonNull(cacheManager.getCache("items")).clear();
    }

    @Nested
    @DisplayName("Поиск всех итемов")
    class FindAll {

        @Test
        @DisplayName("Title не передан")
        void findAll() {
            //GIVEN
            PageRequest inputPage = PageRequest.of(0, 20);
            String cashKey = inputPage + "::" + null;
            //THEN
            service.findAll(inputPage, null)
                    .doOnNext(items -> assertThat(items.getTotalElements()).isEqualTo(17))
                    .block();
            Page<ItemDto> actualCachedPage = Objects.requireNonNull(cacheManager.getCache("items")).get(cashKey, Page.class);
            assertNotNull(actualCachedPage);
            assertThat(actualCachedPage.getTotalElements()).isEqualTo(17);
        }

        @Test
        @DisplayName("Title передан")
        void byTitle() {
            //GIVEN
            String title = "Галстук";
            PageRequest inputPage = PageRequest.of(0, 20);
            String cashKey = inputPage + "::" + title;
            //WHEN
            Mono<Page<ItemDto>> actualResult = service.findAll(inputPage, title);
            //THEN
            StepVerifier.create(actualResult)
                    .assertNext(itemDtos -> {
                        assertThat(itemDtos.getTotalElements()).isEqualTo(1);
                        assertThat(itemDtos.getContent().getFirst().title()).isEqualTo(title);
                    }).verifyComplete();
            Page<ItemDto> actualCachedPage = Objects.requireNonNull(cacheManager.getCache("items"))
                    .get(cashKey, Page.class);
            assertNotNull(actualCachedPage);
            assertThat(actualCachedPage.getTotalElements()).isEqualTo(1);
            assertThat(actualCachedPage.getContent().getFirst().title()).isEqualTo(title);
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

            ItemDto cachedItem = Objects.requireNonNull(cacheManager.getCache("item"))
                    .get(1L, ItemDto.class);
            assertNotNull(cachedItem);
            assertEquals(1L, cachedItem.id());
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
            ItemDto cachedItem = Objects.requireNonNull(cacheManager.getCache("item"))
                    .get(Long.MAX_VALUE, ItemDto.class);
            assertNull(cachedItem);
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
}
