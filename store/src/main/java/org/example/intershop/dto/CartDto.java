package org.example.intershop.dto;

import java.util.List;

public record CartDto(
        Long id,
        String username,
        List<ItemDto> items
) {
}
