package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public Flux<ItemDto> findAll(PageRequest page, String title) {
        if (StringUtils.isBlank(title)) {
            return itemRepository.findAllBy(page)
                    .map(itemMapper::itemEntityToItemDto);
        }
        return itemRepository.findAllByTitle(title, page)
                .map(itemMapper::itemEntityToItemDto);
    }

    @Transactional(readOnly = true)
    public Mono<ItemDto> findById(Long id) {
        return itemRepository.findById(id)
                .map(itemMapper::itemEntityToItemDto)
                .switchIfEmpty(Mono.error(new BusinessException(String.format("Item with id = %s not found", id))));
    }

    @Transactional
    public Mono<ItemDto> action(Long id, String action) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(String.format("Item with id = %s not found", id))))
                .flatMap(foundItem -> {
                    Long calculatedCount = calculateCount(action, foundItem.getCount());
                    foundItem.setCount(calculatedCount);
                    foundItem.setCartId(calculatedCount > 0 ? 1L : null);
                    return itemRepository.save(foundItem);
                }).map(itemMapper::itemEntityToItemDto);
    }

    @Transactional(readOnly = true)
    public Flux<ItemDto> findAllByCartId(Long cartId) {
        return itemRepository.findAllByCartId(cartId)
                .map(itemMapper::itemEntityToItemDto);
    }

    private Long calculateCount(String action, Long currentCount) {
        return switch (action.toLowerCase()) {
            case "minus" -> currentCount > 0 ? --currentCount : 0L;
            case "plus" -> ++currentCount;
            default -> 0L;
        };
    }
}
