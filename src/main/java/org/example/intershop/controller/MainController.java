package org.example.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping
    public String mainPage(
            @RequestParam(name = "sort", defaultValue = "NO") String sort,
            @RequestParam(name = "pageNumber", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "search", required = false) String title,
            Model model
    ) {
        Page<ItemDto> itemPage = itemService.findAll(createPageRequest(page, pageSize, sort), title);
        model.addAttribute("items", ListUtils.partition(itemPage.getContent(), 3));
        model.addAttribute("paging", itemPage);
        return "main";
    }

    private PageRequest createPageRequest(int page, int pageSize, String sort) {
        Sort sorting = switch (sort) {
            case "ALPHA" -> Sort.by("title").ascending();
            case "PRICE" -> Sort.by("price").ascending();
            default -> Sort.unsorted();
        };
        return PageRequest.of(page, pageSize, sorting);
    }
}
