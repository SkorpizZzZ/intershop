package org.example.intershop.service.integration.service;

import org.example.intershop.domain.Order;
import org.example.intershop.dto.OrderItemDto;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.example.intershop.service.OrderItemService;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Nested
    @DisplayName("Сохранение orderItem")
    class SaveOrderItem {

        @Test
        @DisplayName("Успешное сохранение")
        void happyPath() {
            Flux<OrderItemDto> actualResult = itemRepository.findById(1L)
                    .flatMap(item -> {
                        item.setCartId(1L);
                        item.setCount(1L);
                        return itemRepository.save(item);
                    }).zipWith(orderRepository.save(new Order()))
                    .flatMapMany(tuple -> service.saveOrderItems(List.of(tuple.getT1()), tuple.getT2()));
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result.quantity()).isEqualTo(1))
                    .verifyComplete();
        }
    }
}
