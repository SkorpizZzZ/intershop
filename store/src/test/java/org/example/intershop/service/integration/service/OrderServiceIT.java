package org.example.intershop.service.integration.service;

import org.example.intershop.client.HttpPaymentClient;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.repository.CartRepository;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.example.intershop.service.OrderService;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class OrderServiceIT extends AbstractIntegration {
    @Autowired
    private OrderService service;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @MockitoBean
    private HttpPaymentClient paymentClient;

    private Order order = new Order();
    private OrderItem firstOrderItem;
    private OrderItem secondOrderItem;
    private Item firstItem;
    private Item secondItem;


    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll()
                .then(orderRepository.deleteAll())
                .then(itemRepository.resetAllCountsAndCartIds())
                .block();
        order = orderRepository.save(order).block();
        firstItem = itemRepository.findById(1L)
                .doOnNext(item -> {
                    item.setCount(1L);
                    item.setCartId(1L);
                })
                .flatMap(itemRepository::save)
                .block();
        secondItem = itemRepository.findById(2L)
                .doOnNext(item -> {
                    item.setCount(1L);
                    item.setCartId(1L);
                })
                .flatMap(itemRepository::save)
                .block();
        firstOrderItem = OrderItem.builder()
                .itemId(firstItem.getId())
                .quantity(firstItem.getCount())
                .orderId(order.getId())
                .build();
        secondOrderItem = OrderItem.builder()
                .itemId(secondItem.getId())
                .quantity(secondItem.getCount())
                .orderId(order.getId())
                .build();
        firstOrderItem = orderItemRepository.save(firstOrderItem).block();
        secondOrderItem = orderItemRepository.save(secondOrderItem).block();
    }

    @Nested
    @DisplayName("Оформление заказа")
    class Buy {
        @Test
        @DisplayName("Успешная покупка")
        void successBuy() {
            //WHEN
            Mockito.when(paymentClient.pay(any())).thenReturn(Mono.just(new BigDecimal("1000.00")));
            //THEN
            Mono<OrderDto> actualResult = service.buy();
            StepVerifier.create(actualResult)
                    .assertNext(result -> {
                        assertThat(result).isNotNull();
                        assertThat(result.getItems()).hasSize(2);
                    }).verifyComplete();
        }
    }

    @Nested
    @DisplayName("Поиск заказа по id")
    class FindById {
        @Test
        @DisplayName("Заказ найден")
        void orderFound() {
            service.findById(order.getId())
                    .doOnNext(actualResult -> assertThat(actualResult.getItems()).hasSize(2))
                    .block();
        }

        @Test
        @DisplayName("По переданному id заказ не найден")
        void orderNotFound() {
            Mono<OrderDto> actualResult = service.findById(Long.MAX_VALUE);
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                            throwable.getMessage().equals(String.format("Order with id = %s not found", Long.MAX_VALUE)))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Поиск всех заказов")
    class FindAll {
        @Test
        @DisplayName("Все заказы найдены")
        void findAll() {
            Flux<OrderDto> actualResult = service.findAll();
            StepVerifier.create(actualResult.collectList())
                            .assertNext(result -> assertThat(result).hasSize(1));
        }
    }
}