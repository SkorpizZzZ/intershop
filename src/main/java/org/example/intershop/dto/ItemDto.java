package org.example.intershop.dto;

import java.math.BigDecimal;

public record ItemDto(
        Long id,
        String title,
        BigDecimal price,
        String description,
        Long count,
        ImageDto image
) {
}
