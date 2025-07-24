package org.example.intershop.service.integration.service;

import org.example.intershop.domain.Cart;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.User;
import org.example.intershop.dto.OrderItemDto;
import org.example.intershop.enums.Role;
import org.example.intershop.repository.CartRepository;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.example.intershop.repository.UserRepository;
import org.example.intershop.service.OrderItemService;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class OrderItemServiceIT extends AbstractIntegration {
    @Autowired
    private OrderItemService service;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("Сохранение orderItem")
    class SaveOrderItem {

        Cart savedCart;
        Item item;
        Order savedOrder;

        @BeforeEach
        void setUp() {
            User newUser = User.builder()
                    .role(Role.USER)
                    .username("username")
                    .password("123")
                    .build();
            cartRepository.deleteAll()
                    .then(orderRepository.deleteAll())
                    .block();
            userRepository.deleteAll().block();
            userRepository.save(newUser).block();
            Cart newCart = Cart.builder()
                    .username("username")
                    .build();
            savedCart = cartRepository.save(newCart).block();
            item = itemRepository.findById(1L).block();
            item.setCartId(savedCart.getId());
            item.setCount(1L);
            item = itemRepository.save(item).block();
            Order newOrder = Order.builder()
                    .cartId(savedCart.getId())
                    .build();
            savedOrder = orderRepository.save(newOrder).block();
        }

        @Test
        @DisplayName("Успешное сохранение")
        @WithMockUser
        void happyPath() {
            Flux<OrderItemDto> actualResult = service.saveOrderItems(List.of(item), savedOrder);
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result.quantity()).isEqualTo(1));
        }
    }
}
