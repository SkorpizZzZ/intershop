package org.example.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.intershop.domain.Cart;
import org.example.intershop.domain.Item;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.CartRepository;
import org.example.intershop.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public Page<ItemDto> findAll(PageRequest page, String title) {
        if (StringUtils.isBlank(title)) {
            return itemRepository.findAll(page).map(itemMapper::itemEntityToItemDto);
        }
        return itemRepository.findAllByTitle(title, page).map(itemMapper::itemEntityToItemDto);
    }

    @Transactional(readOnly = true)
    public ItemDto findById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item with id = %s not found", id)));
        return itemMapper.itemEntityToItemDto(item);
    }

    @Transactional
    public ItemDto action(Long id, String action) {
        Item foundItem = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item with id = %s not found", id)));
        Long calculatedCount = calculateCount(action, foundItem.getCount());
        foundItem.setCount(calculatedCount);
        Cart cart = cartRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Cart with id = 1 not found"));
        foundItem.setCart(calculatedCount > 0 ? cart : null);
        return itemMapper.itemEntityToItemDto(itemRepository.save(foundItem));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAllByCartId(Long cartId) {
        return itemRepository.findAllByCartId(cartId).stream()
                .map(itemMapper::itemEntityToItemDto)
                .toList();
    }

    private Long calculateCount(String action, Long currentCount) {
        return switch (action.toLowerCase()) {
            case "minus" -> currentCount > 0 ? --currentCount : 0L;
            case "plus" -> ++currentCount;
            default -> 0L;
        };
    }
}
