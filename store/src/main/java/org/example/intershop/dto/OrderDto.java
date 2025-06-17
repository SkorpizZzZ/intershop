package org.example.intershop.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        Long id,
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
