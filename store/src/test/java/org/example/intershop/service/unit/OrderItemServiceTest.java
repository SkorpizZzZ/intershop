package org.example.intershop.service.unit;

import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderItemDto;
import org.example.intershop.mapper.OrderItemMapperImpl;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.example.intershop.service.OrderItemService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderItemServiceTest {
    @InjectMocks
    private OrderItemService service;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Spy
    private OrderItemMapperImpl orderItemMapper;


    @Nested
    @DisplayName("Сохранение orderItems")
    class SaveOrderItems {

        OrderItem savedOrderItem;
        Item inputItem;
        Order inputOrder;

        @BeforeEach
        void setUp() {
            savedOrderItem = OrderItem.builder()
                    .id(1L)
                    .itemId(1L)
                    .orderId(1L)
                    .quantity(1L)
                    .build();
            inputItem = Item.builder()
                    .id(1L)
                    .price(BigDecimal.ONE)
                    .imageName("image")
                    .description("desc")
                    .count(1L)
                    .title("title")
                    .cartId(null)
                    .build();
            inputOrder = new Order(1L, 1L);
        }

        @Test
        @DisplayName("Успешное сохранение")
        void happyPath() {
            //WHEN
            when(orderItemRepository.save(any())).thenReturn(Mono.just(savedOrderItem));
            when(itemRepository.save(any())).thenReturn(Mono.just(inputItem));
            //THEN
            Flux<OrderItemDto> actualResult = service.saveOrderItems(List.of(inputItem), inputOrder);
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isNotNull())
                    .verifyComplete();
        }
    }
}
