package org.example.intershop.dto;

import java.math.BigDecimal;

public record ItemDto(
        Long id,
        String title,
        BigDecimal price,
        String description,
        Long count,
        String imageName,
        Long cartId
) {
    public BigDecimal sumPrice() {
        return price.multiply(BigDecimal.valueOf(count));
    }
}
