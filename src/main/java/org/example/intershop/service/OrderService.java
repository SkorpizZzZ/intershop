package org.example.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.intershop.domain.Item;
import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.mapper.OrderMapper;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.repository.OrderItemRepository;
import org.example.intershop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemService itemService;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;

    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::orderEntityToOrderDto)
                .toList();
    }

    public OrderDto buy() {
        List<Item> itemsInCart = itemRepository.findAllByCartId(1L);
        Order savedOrder = transactionService.doInTransaction(this::saveOrder, itemsInCart);
        OrderDto result = orderMapper.orderEntityToOrderDto(savedOrder);
        transactionService.doInTransaction(this::resetCount, itemsInCart);

        return result;
    }

    private void resetCount(List<Item> itemsInCart) {
        itemsInCart.forEach(item -> item.setCount(0L));
        itemRepository.saveAll(itemsInCart);
    }

    private Order saveOrder(List<Item> itemsInCart) {
        Order order = new Order();
        itemsInCart.forEach(item -> {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .quantity(item.getCount())
                    .build();
            order.getOrderItems().add(orderItem);
            item.setCart(null);
        });
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id = %s not found", id)));
        order.setOrderItems(orderItemRepository.findByOrderId(id));
        return orderMapper.orderEntityToOrderDto(order);
    }
}
