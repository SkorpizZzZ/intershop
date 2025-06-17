package org.example.intershop.service.unit;

import org.example.intershop.domain.Cart;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.dto.OrderItemDto;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.mapper.ItemMapperImpl;
import org.example.intershop.mapper.OrderItemMapperImpl;
import org.example.intershop.mapper.OrderMapperImpl;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.example.intershop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService service;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Spy
    private OrderMapperImpl orderMapper;
    @Spy
    private ItemMapperImpl itemMapper;
    @Spy
    private OrderItemMapperImpl orderItemMapper;


    private Cart cart;
    private Order order;
    private OrderItem orderItem;
    private Item item;

    private ItemDto itemDto;
    private OrderDto orderDto;
    private OrderItemDto orderItemDto;

    @BeforeEach
    void setUp() {
        cart = new Cart(1L);
        order = new Order(1L);
        orderItem = new OrderItem(
                1L,
                null,
                null,
                1L
        );
        itemDto = new ItemDto(
                1L,
                "title",
                BigDecimal.ONE,
                "desc",
                1L,
                "imageName",
                1L
        );
        item = new Item(
                1L,
                "title",
                BigDecimal.ONE,
                "desc",
                1L,
                "imageName",
                1L
        );
        orderItem.setItemId(item.getId());
        orderItem.setOrderId(order.getId());
        item.setCartId(1L);
        orderItemDto = new OrderItemDto(
                1L,
                itemDto,
                1L
        );
        orderDto = new OrderDto(1L, Collections.singletonList(orderItemDto));
    }

    @Nested
    @DisplayName("Поиск заказа по id")
    class FindById {

        Long inputId = 1L;

        @Test
        @DisplayName("Заказ найден")
        void orderFound() {
            //WHEN
            when(orderRepository.findById(anyLong())).thenReturn(Mono.just(order));
            when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.just(orderItem));
            when(itemRepository.findById(anyLong())).thenReturn(Mono.just(item));
            //THEN
            service.findById(inputId)
                    .doOnNext(result -> assertThat(result).isEqualTo(orderDto))
                    .block();
        }

        @Test
        @DisplayName("Заказ не найден")
        void orderNotFound() {
            //WHEN
            when(orderRepository.findById(anyLong())).thenReturn(Mono.empty());
            //THEN
            Mono<OrderDto> actualResult = service.findById(inputId);
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                            throwable.getMessage().equals("Order with id = 1 not found")
                    ).verify();
            verify(orderItemRepository, never()).findByOrderId(anyLong());
            verify(itemRepository, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("Покупка")
    class Buy {

        @Test
        @DisplayName("Успешная покупка")
        void successBuy() {
            //WHEN
            when(itemRepository.findAllByCartId(anyLong())).thenReturn(Flux.just(item));
            when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
            when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(item));
            when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem));
            //THEN
            service.buy().block();
        }
    }

    @Nested
    @DisplayName("Поиск всех заказов")
    class FindAll {

        @Test
        @DisplayName("Заказы найдены")
        void ordersFound() {
            //WHEN
            when(orderRepository.findAll()).thenReturn(Flux.just(order));
            when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.just(orderItem));
            when(itemRepository.findById(anyLong())).thenReturn(Mono.just(item));
            //THEN
            Flux<OrderDto> actualResult = service.findAll();
            StepVerifier.create(actualResult.collectList())
                            .assertNext(result -> {
                                assertThat(result).hasSize(1);
                                assertThat(result).contains(orderDto);
                            }).verifyComplete();
        }
    }
}