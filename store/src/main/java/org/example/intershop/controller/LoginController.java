package org.example.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public Mono<String> login() {
        return Mono.just("login");
    }
}
