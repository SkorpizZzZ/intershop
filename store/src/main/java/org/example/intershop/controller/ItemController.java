package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.example.intershop.security.service.SecurityService;
import org.example.intershop.service.ItemService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/main")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final SecurityService securityService;

    @GetMapping
    public Mono<String> mainPage(
            @RequestParam(name = "sort", defaultValue = "NO") String sort,
            @RequestParam(name = "pageNumber", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "search", required = false) String title,
            Model model
    ) {
        PageRequest pageRequest = createPageRequest(page, pageSize, sort);
        return itemService.findAll(pageRequest, title)
                .zipWith(securityService.isAuthenticated()
                        .defaultIfEmpty(false)
                )
                .flatMap(tuple -> {
                    model.addAttribute("items", ListUtils.partition(tuple.getT1().getContent(), 3));
                    model.addAttribute("paging", tuple.getT1());
                    model.addAttribute("isAuthenticated", tuple.getT2());
                    return Mono.just("main");
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable("id") Long id, Model model) {
        return itemService.findById(id)
                .zipWith(securityService.isAuthenticated()
                        .defaultIfEmpty(false)
                )
                .doOnNext(tuple -> {
                    model.addAttribute("item", tuple.getT1());
                    model.addAttribute("isAuthenticated", tuple.getT2());
                })
                .thenReturn("item");
    }


    @PostMapping("/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> actionItem(
            @PathVariable("id") Long id,
            ServerWebExchange serverWebExchange,
            Model model
    ) {
        return serverWebExchange.getFormData()
                .flatMap(data -> itemService.action(id, data.getFirst("action")))
                .doOnNext(itemDto -> model.addAttribute("item", itemDto))
                .thenReturn("redirect:/main");
    }

    private PageRequest createPageRequest(int page, int pageSize, String sort) {
        Sort sorting = switch (sort) {
            case "ALPHA" -> Sort.by("title").ascending();
            case "PRICE" -> Sort.by("price").ascending();
            default -> Sort.by("id").ascending();
        };
        return PageRequest.of(page, pageSize, sorting);
    }
}
