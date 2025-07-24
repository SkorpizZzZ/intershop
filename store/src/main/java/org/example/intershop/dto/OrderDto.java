package org.example.intershop.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderDto(
        Long id,
        Long cartId,
        List<OrderItemDto> orderItems
) {

    public List<ItemDto> getItems() {
       return orderItems().stream()
                .map(OrderItemDto::item)
                .toList();
    }

    public BigDecimal calculateTotalSum() {
       return orderItems.stream()
               .map(OrderItemDto::sumPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
