package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String findAll(Model model) {
        List<OrderDto> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String findOrder(@PathVariable(name = "id") Long id, Model model) {
        OrderDto foundOrder = orderService.findById(id);
        model.addAttribute("order", foundOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy(RedirectAttributes redirectAttributes) {
        OrderDto order = orderService.buy();
        redirectAttributes.addFlashAttribute("newOrder", true);
        return "redirect:/orders/".concat(order.id().toString());
    }
}
