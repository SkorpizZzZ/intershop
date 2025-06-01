package org.example.intershop.service.integration.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.intershop.domain.Cart;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Order order = new Order();
    private OrderItem firstOrderItem;
    private OrderItem secondOrderItem;
    private Item firstItem;
    private Item secondItem;


    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        orderItemRepository.deleteAll();
        firstItem = itemRepository.findById(1L).get();
        firstItem.setCount(1L);
        secondItem = itemRepository.findById(2L).get();
        secondItem.setCount(1L);
        firstItem = itemRepository.save(firstItem);
        secondItem = itemRepository.save(secondItem);
        firstOrderItem = OrderItem.builder()
                .item(firstItem)
                .quantity(firstItem.getCount())
                .order(order)
                .build();
        secondOrderItem = OrderItem.builder()
                .item(secondItem)
                .quantity(secondItem.getCount())
                .order(order)
                .build();
        order.setOrderItems(List.of(firstOrderItem, secondOrderItem));
        order = orderRepository.save(order);
        firstOrderItem = orderItemRepository.save(firstOrderItem);
        secondOrderItem = orderItemRepository.save(secondOrderItem);
    }

    @Nested
    @DisplayName("Оформление заказа")
    class Buy {
        @Test
        @DisplayName("Успешная покупука")
        @Transactional
        void successBuy() {
            //GIVEN
            Item firstItem = itemRepository.findById(1L).get();
            Item secondItem = itemRepository.findById(2L).get();
            Cart cart = cartRepository.findById(1L).get();
            cart.setItems(List.of(firstItem, secondItem));
            firstItem.setCart(cart);
            secondItem.setCart(cart);
            itemRepository.saveAll(List.of(firstItem, secondItem));
            //THEN
            OrderDto actualResult = service.buy();
            assertThat(actualResult).isNotNull();
            assertThat(actualResult.getItems()).size().isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Поиск заказа по id")
    class FindById {
        @Test
        @DisplayName("Заказ найден")
        void orderFound() {
            OrderDto actualResult = service.findById(order.getId());
            assertThat(actualResult.getItems()).size().isEqualTo(2L);
        }
        @Test
        @DisplayName("По переданному id заказ не найден")
        void orderNotFound() {
            assertThatThrownBy(() -> service.findById(Long.MAX_VALUE))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(String.format("Order with id = %s not found", Long.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("Поиск всех заказов")
    class FindAll {
        @Test
        @DisplayName("Все заказы найдены")
        void findAll() {
            List<OrderDto> actualResult = service.findAll();
            assertThat(actualResult).size().isEqualTo(1);
        }
    }
}