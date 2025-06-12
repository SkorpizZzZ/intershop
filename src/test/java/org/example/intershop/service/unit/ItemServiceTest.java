//package org.example.intershop.service.unit;
//
//import jakarta.persistence.EntityNotFoundException;
//import org.example.intershop.domain.Cart;
//import org.example.intershop.domain.Item;
//import org.example.intershop.domain.Order;
//import org.example.intershop.domain.OrderItem;
//import org.example.intershop.dto.ItemDto;
//import org.example.intershop.mapper.ItemMapperImpl;
//import org.example.intershop.repository.CartRepository;
//import org.example.intershop.repository.ItemRepository;
//import org.example.intershop.service.ItemService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ItemServiceTest {
//
//    @InjectMocks
//    private ItemService service;
//    @Mock
//    private ItemRepository itemRepository;
//    @Mock
//    private CartRepository cartRepository;
//    @Spy
//    private ItemMapperImpl mapper;
//
//    private Cart cart;
//    private Order order;
//    private OrderItem orderItem;
//    private ItemDto itemDto;
//
//    private Item item;
//
//
//    @BeforeEach
//    void setUp() {
//        cart = new Cart(1L, Collections.emptyList());
//        order = new Order(1L, Collections.emptyList());
//        orderItem = new OrderItem(
//                1L,
//                null,
//                null,
//                1L
//        );
//        itemDto = new ItemDto(
//                1L,
//                "title",
//                BigDecimal.ONE,
//                "desc",
//                1L,
//                "imageName",
//                1L
//        );
//        item = new Item(
//                1L,
//                "title",
//                BigDecimal.ONE,
//                "desc",
//                1L,
//                "imageName",
//                null,
//                null
//        );
//        cart.setItems(Collections.singletonList(item));
//        order.setOrderItems(Collections.singletonList(orderItem));
//        orderItem.setItem(item);
//        orderItem.setOrder(order);
//        item.setCart(cart);
//        item.setOrderItems(Collections.singletonList(orderItem));
//    }
//
//
//    @Nested
//    @DisplayName("Поиск всех итемов")
//    class FindAll {
//        PageRequest page = PageRequest.of(0, 1, Sort.unsorted());
//        String title = null;
//
//
//        @Test
//        @DisplayName("Заголовок не передан")
//        void titleIsBlank() {
//            //GIVEN
//            Flux<Item> mockItem = Flux.just(item);
//            //WHEN
//            when(itemRepository.findAllBy(any(Pageable.class))).thenReturn(mockItem);
//            //THEN
//            Flux<ItemDto> actualResult = service.findAll(page, title);
//            StepVerifier.create(actualResult.collectList())
//                    .assertNext(result -> {
//                        assertThat(result).hasSize(1);
//                        assertThat(result.getFirst()).isEqualTo(itemDto);
//                    }).verifyComplete();
//            verify(itemRepository, never()).findAllByTitle(anyString(), any(Pageable.class));
//        }
//
//        @Test
//        @DisplayName("Поиск по заголовку")
//        void titleIsNotBlank() {
//            //GIVEN
//            title = "title";
//            Flux<Item> mockItem = Flux.just(item);
//            //WHEN
//            when(itemRepository.findAllByTitle(anyString(), any(Pageable.class))).thenReturn(mockItem);
//            //THEN
//            Flux<ItemDto> actualResult = service.findAll(page, title);
//            StepVerifier.create(actualResult.collectList())
//                    .assertNext(result -> {
//                        assertThat(result).hasSize(1);
//                        assertThat(result.getFirst()).isEqualTo(itemDto);
//                    }).verifyComplete();
//            verify(itemRepository, never()).findAllBy(any(Pageable.class));
//        }
//
//        @Test
//        @DisplayName("Поиск не дал результатов")
//        void itemsNotFound() {
//            //GIVEN
//            Flux<Item> empty = Flux.empty();
//            //WHEN
//            when(itemRepository.findAllBy(any(Pageable.class))).thenReturn(empty);
//            //THEN
//            Flux<ItemDto> actualResult = service.findAll(page, title);
//            StepVerifier.create(actualResult.collectList())
//                    .assertNext(result -> assertThat(result).isEmpty())
//                    .verifyComplete();
//            verify(itemRepository, never()).findAllByTitle(anyString(), any(Pageable.class));
//        }
//    }
//
//    @Nested
//    @DisplayName("Поиск по id")
//    class FindById {
//        Long inputId = 1L;
//
//        @Test
//        @DisplayName("Итем найден")
//        void itemFound() {
//            //WHEN
//            when(itemRepository.findById(anyLong())).thenReturn(Mono.just(item));
//            //THEN
//            service.findById(inputId)
//                    .doOnNext(actualResult -> assertThat(actualResult).isEqualTo(itemDto))
//                    .block();
//        }
//
//        @Test
//        @DisplayName("Итем по переданному айди не найден")
//        void itemNotFound() {
//            //WHEN
//            when(itemRepository.findById(anyLong())).thenReturn(Mono.empty());
//            //THEN
//            Mono<ItemDto> actualResult = service.findById(inputId);
//
//            StepVerifier.create(actualResult)
//                    .expectErrorMatches(result ->
//                            result instanceof EntityNotFoundException &&
//                            result.getMessage().equals("Item with id = 1 not found"))
//                    .verify();
//        }
//    }
//
//    @Nested
//    @DisplayName("Убрать/добавить в корзину")
//    class Action {
//        Long inputItemId;
//        String inputAction;
//
//        @BeforeEach
//        void setUp() {
//            inputItemId = 1L;
//            inputAction = null;
//        }
//
//        @Test
//        @DisplayName("Итем не найден")
//        void itemNotFound() {
//            //WHEN
//            when(itemRepository.findById(anyLong())).thenReturn(Mono.empty());
//            when(cartRepository.findById(anyLong())).thenReturn(Mono.just(cart));
//            //THEN
//            Mono<ItemDto> actualResult = service.action(inputItemId, inputAction);
//
//            StepVerifier.create(actualResult)
//                    .expectErrorMatches(throwable ->
//                            throwable instanceof EntityNotFoundException &&
//                            throwable.getMessage().equals("Item with id = 1 not found")
//                    ).verify();
//        }
//
//        @Test
//        @DisplayName("Добавление экземпляра в корзину")
//        void plus() {
//            //GIVEN
//            inputAction = "plus";
//            ItemDto expectedResult = new ItemDto(
//                    1L,
//                    "title",
//                    BigDecimal.ONE,
//                    "desc",
//                    2L,
//                    "imageName",
//                    1L
//            );
//            //WHEN
//            when(itemRepository.findById(anyLong())).thenReturn(Mono.just(item));
//            when(cartRepository.findById(anyLong())).thenReturn(Mono.just(cart));
//            when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(item));
//            //THEN
//            Mono<ItemDto> actualResult = service.action(inputItemId, inputAction);
//            StepVerifier.create(actualResult)
//                    .assertNext(item -> {
//                        assertThat(item).isEqualTo(expectedResult);
//                        assertThat(item.count()).isEqualTo(2L);
//                    }).verifyComplete();
//        }
//
//        @Test
//        @DisplayName("Удаление экземпляра из корзины")
//        void minus() {
//            //GIVEN
//            inputAction = "minus";
//            ItemDto expectedResult = new ItemDto(
//                    1L,
//                    "title",
//                    BigDecimal.ONE,
//                    "desc",
//                    0L,
//                    "imageName",
//                    null
//            );
//            item.setCart(null);
//            item.setCount(0L);
//            //WHEN
//            when(itemRepository.findById(anyLong())).thenReturn(Mono.just(item));
//            when(cartRepository.findById(anyLong())).thenReturn(Mono.just(cart));
//            when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(item));
//            //THEN
//            Mono<ItemDto> actualResult = service.action(inputItemId, inputAction);
//            StepVerifier.create(actualResult)
//                    .assertNext(item -> {
//                        assertThat(item).isEqualTo(expectedResult);
//                        assertThat(item.count()).isEqualTo(0L);
//                    }).verifyComplete();
//        }
//    }
//
//    @Nested
//    @DisplayName("Поиск Итема по айдишнику корзины")
//    class FindAllByCartId {
//        Long inputId = 1L;
//
//        @Test
//        @DisplayName("Итемы найден")
//        void itemsFound() {
//            //WHEN
//            when(itemRepository.findAllByCartId(anyLong())).thenReturn(Flux.just(item));
//            //THEN
//            Flux<ItemDto> actualResult = service.findAllByCartId(inputId);
//            StepVerifier.create(actualResult.collectList())
//                    .assertNext(result -> assertThat(result).contains(itemDto)
//                    ).verifyComplete();
//        }
//    }
//}
