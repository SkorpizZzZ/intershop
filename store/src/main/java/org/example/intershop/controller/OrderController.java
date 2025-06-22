package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> findAll(Model model) {
        return orderService.findAll()
                .collectList()
                .flatMap(orders -> {
                    model.addAttribute("orders", orders);
                    return Mono.just("orders");
                });
    }

    @GetMapping("/{id}")
    public Mono<String> findOrder(
            @PathVariable(name = "id") Long id,
            ServerWebExchange serverWebExchange,
            Model model) {
        String newOrder = serverWebExchange.getRequest().getQueryParams().getFirst("newOrder");

        if (newOrder != null) {
            model.addAttribute("newOrder", newOrder);
        }
        return orderService.findById(id)
                .doOnNext(order -> model.addAttribute("order", order))
                .thenReturn("order");
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.buy()
                .map(order -> "redirect:/orders/".concat(order.id().toString().concat("?newOrder=true")))
                .onErrorResume(BusinessException.class, e -> {
                    String error = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                    return Mono.just("redirect:/cart/items".concat("?error=").concat(error));
                });
    }
}
