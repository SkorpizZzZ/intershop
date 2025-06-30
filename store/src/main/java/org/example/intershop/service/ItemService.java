package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.intershop.dto.ItemDto;
import org.example.intershop.dto.RestPage;
import org.example.intershop.exception.BusinessException;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
                .map(tuple -> new PageImpl<>(tuple.getT1(), page, tuple.getT2()));
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
    @Cacheable(value = "items", key = "#cartId")
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
