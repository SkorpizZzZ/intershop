package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ItemService itemService;

    @GetMapping("/items")
    public String getCart(Model model) {
        List<ItemDto> items = itemService.findAllByCartId(1L);
        model.addAttribute("items", items);
        BigDecimal total = items.stream()
                .map(ItemDto::sumPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/items/{id}")
    public String actionItem(
            @PathVariable("id") Long id,
            @RequestParam(name = "action") String action,
            Model model
    ) {
        ItemDto actionItem = itemService.action(id, action);
        model.addAttribute("item", actionItem);
        return "redirect:/cart/items";
    }
}
