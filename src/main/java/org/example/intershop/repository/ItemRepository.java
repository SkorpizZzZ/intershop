package org.example.intershop.repository;

import org.example.intershop.domain.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends R2dbcRepository<Item, Long> {

    Flux<Item> findAllByTitle(String title, Pageable pageable);

    Flux<Item> findAllByCartId(Long cartId);

    Flux<Item> findAllBy(Pageable pageable);
}
