package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ItemService itemService;

    @GetMapping("/items")
    public Mono<String> getCart(Model model) {
        return itemService.findAllByCartId(1L)
                .collectList()
                .flatMap(items -> {
                    model.addAttribute("items", items);
                    BigDecimal total = items.stream()
                            .map(ItemDto::sumPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    model.addAttribute("total", total);
                    return Mono.just("cart");
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> actionItem(
            @PathVariable("id") Long id,
            ServerWebExchange serverWebExchange,
            Model model
    ) {
        return  serverWebExchange.getFormData()
                .flatMap(data -> itemService.action(id, data.toSingleValueMap().get("action")))
                .doOnNext(itemDto -> model.addAttribute("item", itemDto))
                .thenReturn("redirect:/cart/items");
    }
}
