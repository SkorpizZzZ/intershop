package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.intershop.client.HttpPaymentClient;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ItemService itemService;
    private final HttpPaymentClient paymentClient;

    @GetMapping("/items")
    public Mono<String> getCart(Model model, ServerWebExchange serverWebExchange) {
        String error = serverWebExchange.getRequest().getQueryParams().getFirst("error");
        if (StringUtils.isNotBlank(error)) {
            String decodedError = URLDecoder.decode(error, StandardCharsets.UTF_8);
            model.addAttribute("error", decodedError);
        }
        return paymentClient.isPaymentServiceUp()
                .zipWith(itemService.findAllByCartId(1L).collectList())
                .map(tuple -> {
                    Boolean isPaymentUp = tuple.getT1();
                    List<ItemDto> items = tuple.getT2();
                    model.addAttribute("isPaymentUp", isPaymentUp);
                    model.addAttribute("items", items);
                    BigDecimal total = items.stream()
                            .map(ItemDto::sumPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    model.addAttribute("total", total);
                    return "cart";
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> actionItem(
            @PathVariable("id") Long id,
            ServerWebExchange serverWebExchange,
            Model model
    ) {
        return serverWebExchange.getFormData()
                .flatMap(data -> itemService.action(id, data.toSingleValueMap().get("action")))
                .doOnNext(itemDto -> model.addAttribute("item", itemDto))
                .thenReturn("redirect:/cart/items");
    }
}
