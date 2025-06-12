//package org.example.intershop.service.integration.service;
//
//import jakarta.persistence.EntityNotFoundException;
//import org.example.intershop.domain.Cart;
//import org.example.intershop.domain.Item;
//import org.example.intershop.domain.Order;
//import org.example.intershop.domain.OrderItem;
//import org.example.intershop.dto.OrderDto;
//import org.example.intershop.repository.CartRepository;
//import org.example.intershop.repository.ItemRepository;
//import org.example.intershop.repository.OrderItemRepository;
//import org.example.intershop.repository.OrderRepository;
//import org.example.intershop.service.OrderService;
//import org.example.intershop.service.integration.AbstractIntegration;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.annotation.Transactional;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//public class OrderServiceIT extends AbstractIntegration {
//    @Autowired
//    private OrderService service;
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private ItemRepository itemRepository;
//    @Autowired
//    private OrderItemRepository orderItemRepository;
//    @Autowired
//    private CartRepository cartRepository;
//
//    private Order order = new Order();
//    private OrderItem firstOrderItem;
//    private OrderItem secondOrderItem;
//    private Item firstItem;
//    private Item secondItem;
//
//
//    @BeforeEach
//    void setUp() {
//        orderRepository.deleteAll().subscribe();
//        orderItemRepository.deleteAll().subscribe();
//        firstItem = itemRepository.findById(1L)
//                .doOnNext(item -> item.setCount(1L))
//                .flatMap(itemRepository::save)
//                .block();
//        secondItem = itemRepository.findById(2L)
//                .doOnNext(item -> item.setCount(1L))
//                .flatMap(itemRepository::save)
//                .block();
//        firstOrderItem = OrderItem.builder()
//                .item(firstItem)
//                .quantity(firstItem.getCount())
//                .order(order)
//                .build();
//        secondOrderItem = OrderItem.builder()
//                .item(secondItem)
//                .quantity(secondItem.getCount())
//                .order(order)
//                .build();
//        firstOrderItem = orderItemRepository.save(firstOrderItem).block();
//        secondOrderItem = orderItemRepository.save(secondOrderItem).block();
//        order.setOrderItems(List.of(firstOrderItem, secondOrderItem));
//        order = orderRepository.save(order).block();
//    }
//
//    @Nested
//    @DisplayName("Оформление заказа")
//    class Buy {
//        @Test
//        @DisplayName("Успешная покупка")
//        @Transactional
//        void successBuy() {
//            //GIVEN
//            Mono<Void> preparation = cartRepository.findById(1L)
//                    .flatMap(foundCart -> {
//                        foundCart.setItems(List.of(firstItem, secondItem));
//                        firstItem.setCart(foundCart);
//                        secondItem.setCart(foundCart);
//                        return itemRepository.saveAll(List.of(firstItem, secondItem)).then();
//                    });
//            //THEN
//            Mono<OrderDto> actualResult = preparation.then(service.buy());
//            StepVerifier.create(actualResult)
//                    .assertNext(result -> {
//                        assertThat(result).isNotNull();
//                        assertThat(result.getItems()).hasSize(2);
//                    }).verifyComplete();
//        }
//    }
//
//    @Nested
//    @DisplayName("Поиск заказа по id")
//    class FindById {
//        @Test
//        @DisplayName("Заказ найден")
//        void orderFound() {
//            service.findById(order.getId())
//                    .doOnNext(actualResult -> assertThat(actualResult.getItems()).hasSize(2))
//                    .block();
//        }
//
//        @Test
//        @DisplayName("По переданному id заказ не найден")
//        void orderNotFound() {
//            Mono<OrderDto> actualResult = service.findById(Long.MAX_VALUE);
//            StepVerifier.create(actualResult)
//                    .expectErrorMatches(throwable ->
//                            throwable instanceof EntityNotFoundException &&
//                            throwable.getMessage().equals(String.format("Order with id = %s not found", Long.MAX_VALUE)))
//                    .verify();
//        }
//    }
//
//    @Nested
//    @DisplayName("Поиск всех заказов")
//    class FindAll {
//        @Test
//        @DisplayName("Все заказы найдены")
//        void findAll() {
//            Flux<OrderDto> actualResult = service.findAll();
//            StepVerifier.create(actualResult.collectList())
//                            .assertNext(result -> assertThat(result).hasSize(1));
//        }
//    }
//}