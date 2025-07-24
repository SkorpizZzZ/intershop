package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.example.intershop.dto.UserDto;
import org.example.intershop.dto.UserRegistrationDto;
import org.example.intershop.enums.Role;
import org.example.intershop.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public Mono<String> create(@ModelAttribute UserRegistrationDto userDto) {
        return userService.create(userDto.username(), passwordEncoder.encode(userDto.password()), userDto.role())
                .thenReturn("redirect:/login");
    }

    @GetMapping("/registration")
    public Mono<String> registration(Model model, @ModelAttribute("user") UserDto userDto) {
        model.addAttribute("user", userDto);
        model.addAttribute("roles", Role.values());
        return Mono.just("registration");
    }
}
