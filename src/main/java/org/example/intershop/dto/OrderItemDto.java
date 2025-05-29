package org.example.intershop.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long id,
        ItemDto item,
        Long quantity
) {
    public BigDecimal sumPrice() {
        return item.price().multiply(BigDecimal.valueOf(quantity));
    }
}
