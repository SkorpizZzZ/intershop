package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.dto.OrderItemDto;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.mapper.OrderItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    @Transactional(readOnly = true)
    public Flux<OrderDto> findAll() {
        return orderRepository.findAll()
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                                .map(item -> orderItemMapper.orderItemToOrderItemDto(orderItem, item))
                        )
                        .collectList()
                        .map(orderItems -> new OrderDto(order.getId(), orderItems))
                );
    }

    public Mono<OrderDto> buy() {
        return itemRepository.findAllByCartId(1L)
                .collectList()
                .flatMap(this::saveOrder);

    }

    private Mono<OrderDto> saveOrder(List<Item> itemsInCart) {
        return orderRepository.save(new Order())
                .flatMap(savedOrder -> {
                    List<OrderItemDto> savedOrderItems = new ArrayList<>();
                    return Flux.fromIterable(itemsInCart)
                            .flatMap(item -> {
                                item.setCartId(null);
                                OrderItem orderItem = OrderItem.builder()
                                        .orderId(savedOrder.getId())
                                        .itemId(item.getId())
                                        .quantity(item.getCount())
                                        .build();
                                item.setCount(0L);
                                return Mono.zip(
                                                itemRepository.save(item),
                                                orderItemRepository.save(orderItem)
                                        ).map(Tuple2::getT2)
                                        .doOnNext(savedOrderItem ->
                                                savedOrderItems.add(orderItemMapper.orderItemToOrderItemDto(
                                                        savedOrderItem,
                                                        item
                                                ))
                                        );
                            })
                            .then(Mono.just(new OrderDto(savedOrder.getId(), savedOrderItems)));
                });
    }

    @Transactional(readOnly = true)
    public Mono<OrderDto> findById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(String.format("Order with id = %s not found", id))))
                .flatMap(order ->
                        orderItemRepository.findByOrderId(id)
                                .flatMap(foundOrderItem ->
                                        itemRepository.findById(foundOrderItem.getItemId())
                                                .map(itemDto -> orderItemMapper.orderItemToOrderItemDto(foundOrderItem, itemDto))
                                )
                                .collectList()
                                .map(orderItemDtos -> new OrderDto(order.getId(), orderItemDtos))
                );
    }
}
