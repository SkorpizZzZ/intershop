package org.example.intershop.redis;

import org.example.intershop.dto.ItemDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.LongFunction;

@Service
public class ItemRedisService {

    @Cacheable(value = "item", key = "#id")
    public Mono<ItemDto> findById(Long id, LongFunction<Mono<ItemDto>> item) {
        return item.apply(id);
    }

    @Cacheable(value = "items", key = "#pageable")
    public Mono<Page<ItemDto>> findAll(
            PageRequest pageable,
            String title,
            BiFunction<PageRequest, String, Mono<Page<ItemDto>>> items
    ) {
        return items.apply(pageable, title);
    }

    @Cacheable(value = "itemsInCart", key = "#cartId")
    public Flux<ItemDto> findAllByCartId(Long cartId, LongFunction<Flux<ItemDto>> findAllByCartId) {
        return findAllByCartId.apply(cartId);
    }
}
