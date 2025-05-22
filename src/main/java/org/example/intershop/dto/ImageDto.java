package org.example.intershop.dto;

public record ImageDto(
        Long id,
        String path,
        ItemDto itemDto
) {
}
