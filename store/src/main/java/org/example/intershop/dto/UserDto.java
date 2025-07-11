package org.example.intershop.dto;

import org.example.intershop.enums.Role;

public record UserDto(
        String username,
        Role role
) {
}
