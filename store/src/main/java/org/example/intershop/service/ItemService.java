package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.intershop.domain.Item;
import org.example.intershop.dto.CartDto;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.dto.RestPage;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.CartRepository;
import org.example.intershop.repository.ItemRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final ItemMapper itemMapper;

    private final CartService cartService;

    @Transactional(readOnly = true)
    @Cacheable(value = "items", key = "#page + '::' + #title")
    public Mono<Page<ItemDto>> findAll(PageRequest page, String title) {
        if (StringUtils.isBlank(title)) {
            return itemRepository.findAllBy(page)
                    .map(itemMapper::itemEntityToItemDto)
                    .collectList()
                    .zipWith(itemRepository.count())
                    .map(tuple -> new RestPage<>(
                            tuple.getT1(),
                            page.getPageNumber(),
                            page.getPageSize(),
                            tuple.getT2())
                    );
        }
        return itemRepository.findAllByTitleIgnoreCase(title, page)
                .map(itemMapper::itemEntityToItemDto)
                .collectList()
                .zipWith(itemRepository.countAllByTitleIgnoreCase(title))
                .map(tuple -> new RestPage<>(
                        tuple.getT1(),
                        page.getPageNumber(),
                        page.getPageSize(),
                        tuple.getT2())
                );
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "item", key = "#id")
    public Mono<ItemDto> findById(Long id) {
        return itemRepository.findById(id)
                .map(itemMapper::itemEntityToItemDto)
                .switchIfEmpty(Mono.error(new BusinessException(String.format("Item with id = %s not found", id))));
    }

    @Transactional
    @CacheEvict(value = "items", allEntries = true)
    public Mono<ItemDto> action(Long id, String action) {
        return cartService.getCart()
                .map(CartDto::id)
                .flatMap(cartId -> itemRepository.findById(id)
                            .flatMap(item -> updateItem(item, cartId, action))
                );
    }

    @Transactional
    public Mono<ItemDto> updateItem(Item item, Long cartId, String action) {
        Long calculatedCount = calculateCount(action, item.getCount());
        item.setCount(calculatedCount);
        item.setCartId(calculatedCount > 0 ? cartId : null);
        return itemRepository.save(item)
                .map(itemMapper::itemEntityToItemDto);
    }

    private Long calculateCount(String action, Long currentCount) {
        return switch (action.toLowerCase()) {
            case "minus" -> currentCount > 0 ? --currentCount : 0L;
            case "plus" -> ++currentCount;
            case "current" -> currentCount;
            default -> 0L;
        };
    }
}
