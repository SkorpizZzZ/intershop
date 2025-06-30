package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.intershop.client.HttpPaymentClient;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.exception.PaymentException;
import org.example.intershop.mapper.OrderItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    private final OrderItemService orderItemService;
    private final HttpPaymentClient paymentClient;

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

    @Transactional
    @CacheEvict(value = "items", allEntries = true)
    public Mono<OrderDto> buy() {
        return itemRepository.findAllByCartId(1L)
                .collectList()
                .flatMap(this::processOrder);
    }

    private Mono<OrderDto> processOrder(List<Item> itemsInCart) {
        return Mono.defer(() -> {
            BigDecimal totalSum = calculateTotalSum(itemsInCart);
            return paymentClient.pay(totalSum)
                    .flatMap(paymentResult -> saveOrder(itemsInCart))
                    .onErrorResume(PaymentException.class, e ->
                            Mono.error(new BusinessException(e.getMessage())));
        });
    }

    private BigDecimal calculateTotalSum(List<Item> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<OrderDto> saveOrder(List<Item> itemsInCart) {
        return orderRepository.save(new Order())
                .flatMap(savedOrder -> orderItemService.saveOrderItems(itemsInCart, savedOrder)
                        .collectList()
                        .map(savedOrderItems -> new OrderDto(savedOrder.getId(), savedOrderItems))
                );
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
