package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.example.intershop.redis.ItemRedisService;
import org.example.intershop.service.ItemService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ItemRedisService itemRedisService;

    @GetMapping
    public Mono<String> mainPage(
            @RequestParam(name = "sort", defaultValue = "NO") String sort,
            @RequestParam(name = "pageNumber", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "search", required = false) String title,
            Model model
    ) {
        PageRequest pageRequest = createPageRequest(page, pageSize, sort);
        return itemRedisService.findAll(pageRequest, title, itemService::findAll)
                .flatMap(resultPage -> {
                    model.addAttribute("items", ListUtils.partition(resultPage.getContent(), 3));
                    model.addAttribute("paging", resultPage);
                    return Mono.just("main");
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable("id") Long id, Model model) {
        return itemRedisService.findById(id, itemService::findById)
                .doOnNext(itemDto -> model.addAttribute("item", itemDto))
                .thenReturn("item");
    }


    @PostMapping("/items/{id}")
    public Mono<String> actionItem(
            @PathVariable("id") Long id,
            ServerWebExchange serverWebExchange,
            Model model
    ) {
        return  serverWebExchange.getFormData()
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
