package org.example.intershop.repository;

import org.example.intershop.domain.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemRepository extends R2dbcRepository<Item, Long> {

    Flux<Item> findAllByTitleIgnoreCase(String title, Pageable pageable);
    Mono<Long> countAllByTitleIgnoreCase(String title);

    Flux<Item> findAllByCartId(Long cartId);

    Flux<Item> findAllBy(Pageable pageable);
}
