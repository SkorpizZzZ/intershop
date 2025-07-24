package org.example.intershop.dto;

public record CartItemDto(
        Long id,
        Long cartId,
        Long itemId,
        Long quantity
) {
}
