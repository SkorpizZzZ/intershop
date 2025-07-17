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
    private final CartService cartService;
    private final HttpPaymentClient paymentClient;

    @Transactional(readOnly = true)
    public Flux<OrderDto> findAll() {
        return cartService.getCart()
                .flatMapMany(cart -> orderRepository.findAllByCartId(cart.id())
                        .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                                .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                                        .map(item -> orderItemMapper.orderItemToOrderItemDto(orderItem, item))
                                )
                                .collectList()
                                .map(orderItems -> OrderDto.builder()
                                        .id(order.getId())
                                        .cartId(cart.id())
                                        .orderItems(orderItems)
                                        .build()
                                ))
                );
    }

    @Transactional
    @CacheEvict(value = "items", allEntries = true)
    public Mono<OrderDto> buy() {
        return cartService.getCart()
                .flatMap(cart -> itemRepository.findAllByCartId(cart.id())
                        .collectList()
                        .flatMap(items -> processOrder(items, cart.id())));
    }

    private Mono<OrderDto> processOrder(List<Item> itemsInCart, Long cartId) {
        return Mono.defer(() -> {
            BigDecimal totalSum = calculateTotalSum(itemsInCart);
            return paymentClient.pay(totalSum)
                    .flatMap(paymentResult -> saveOrder(itemsInCart, cartId))
                    .onErrorResume(PaymentException.class, e ->
                            Mono.error(new BusinessException(e.getMessage())));
        });
    }

    private BigDecimal calculateTotalSum(List<Item> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<OrderDto> saveOrder(List<Item> itemsInCart, Long cartId) {
        return orderRepository.save(Order.builder()
                        .cartId(cartId)
                        .build()
                )
                .flatMap(savedOrder -> orderItemService.saveOrderItems(itemsInCart, savedOrder)
                        .collectList()
                        .map(savedOrderItems -> OrderDto.builder()
                                .id(savedOrder.getId())
                                .cartId(cartId)
                                .orderItems(savedOrderItems)
                                .build()
                        )
                );
    }

    @Transactional(readOnly = true)
    public Mono<OrderDto> findById(Long id) {
        return cartService.getCart()
                .flatMap(cart -> orderRepository.findByIdAndCartId(id, cart.id())
                        .switchIfEmpty(Mono.error(new BusinessException(String.format("Order with id = %s not found", id))))
                        .flatMap(order ->
                                orderItemRepository.findByOrderId(id)
                                        .flatMap(foundOrderItem ->
                                                itemRepository.findById(foundOrderItem.getItemId())
                                                        .map(itemDto -> orderItemMapper.orderItemToOrderItemDto(foundOrderItem, itemDto))
                                        )
                                        .collectList()
                                        .map(orderItemDtos -> OrderDto.builder()
                                                .id(order.getId())
                                                .cartId(order.getCartId())
                                                .orderItems(orderItemDtos)
                                                .build()
                                        )
                        )
                );
    }
}
