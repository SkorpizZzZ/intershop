package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderItemDto;
import org.example.intershop.mapper.OrderItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public Flux<OrderItemDto> saveOrderItems(List<Item> itemsInCart, Order savedOrder) {
        return Flux.fromIterable(itemsInCart)
                .flatMap(item -> saveOrderItem(savedOrder, item));
    }

    private Mono<OrderItemDto> saveOrderItem(Order savedOrder, Item item) {
        item.setCartId(null);
        OrderItem orderItem = OrderItem.builder()
                .orderId(savedOrder.getId())
                .itemId(item.getId())
                .quantity(item.getCount())
                .build();
        item.setCount(0L);
        return Mono.zip(itemRepository.save(item), orderItemRepository.save(orderItem))
                .map(Tuple2::getT2)
                .map(savedOrderItem ->
                        orderItemMapper.orderItemToOrderItemDto(
                                savedOrderItem,
                                item
                        ));
    }
}
