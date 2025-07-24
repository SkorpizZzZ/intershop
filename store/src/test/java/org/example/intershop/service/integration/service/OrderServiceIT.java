package org.example.intershop.service.integration.service;

import org.example.intershop.client.HttpPaymentClient;
import org.example.intershop.domain.*;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.enums.Role;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.repository.*;
import org.example.intershop.service.OrderService;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@WithMockUser(username = "OrderServiceIT")
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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @MockitoBean
    private HttpPaymentClient paymentClient;

    private Order order;
    private Cart cart;

    @BeforeEach
    void setUp() {
        System.out.println("Cleaning database...");
        itemRepository.resetAllCountsAndCartIds().block();
        cartRepository.deleteAll().block();
        orderItemRepository.deleteAll().block();
        userRepository.deleteAll().block();
        cartItemRepository.deleteAll().block();
        System.out.println("Creating test data...");

        User newUser = userRepository.save(User.builder()
                .role(Role.USER)
                .username("OrderServiceIT")
                .password("123")
                .build()).block();
        assertThat(newUser).isNotNull();

        Cart newCart = Cart.builder()
                .username("OrderServiceIT")
                .build();
        cart = cartRepository.save(newCart).block();
        assertThat(cart).isNotNull();


        Order newOrder = Order.builder()
                .cartId(cart.getId())
                .build();
        order = orderRepository.save(newOrder).block();
        assertThat(order).isNotNull();

        Item firstItem = itemRepository.findById(1L)
                .doOnNext(item -> {
                    item.setCount(1L);
                    item.setCartId(cart.getId());
                })
                .flatMap(itemRepository::save)
                .block();
        assertThat(firstItem).isNotNull();


        Item secondItem = itemRepository.findById(2L)
                .doOnNext(item -> {
                    item.setCount(1L);
                    item.setCartId(cart.getId());
                })
                .flatMap(itemRepository::save)
                .block();
        assertThat(secondItem).isNotNull();

        orderItemRepository.save(OrderItem.builder()
                .itemId(firstItem.getId())
                .quantity(firstItem.getCount())
                .orderId(order.getId())
                .build()).block();
        orderItemRepository.save(OrderItem.builder()
                .itemId(secondItem.getId())
                .quantity(secondItem.getCount())
                .orderId(order.getId())
                .build()).block();
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
                    });
        }
    }

    @Nested
    @DisplayName("Поиск заказа по id")
    class FindById {
        @Test
        @DisplayName("Заказ найден")
        void orderFound() {
            Mono<OrderDto> actualResult = service.findById(order.getId());
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result.getItems()).hasSize(2));
        }

        @Test
        @DisplayName("По переданному id заказ не найден")
        void orderNotFound() {
            Mono<OrderDto> actualResult = service.findById(Long.MAX_VALUE);
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                            throwable.getMessage().equals(String.format(
                                    "Order with id = %s and cartId = %s not found",
                                    Long.MAX_VALUE,
                                    cart.getId()))
                    );
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
                    .assertNext(result -> assertThat(result).hasSize(1))
                    .verifyComplete();
        }
    }
}