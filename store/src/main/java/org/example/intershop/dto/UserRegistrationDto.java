package org.example.intershop.dto;

import org.example.intershop.enums.Role;

public record UserRegistrationDto(
        String username,
        String password,
        Role role
) {
}
