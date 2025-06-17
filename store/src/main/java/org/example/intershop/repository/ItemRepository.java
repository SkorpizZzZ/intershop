package org.example.intershop.repository;

import org.example.intershop.domain.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemRepository extends R2dbcRepository<Item, Long> {

    Flux<Item> findAllByTitleIgnoreCase(String title, Pageable pageable);
    Mono<Long> countAllByTitleIgnoreCase(String title);

    Flux<Item> findAllByCartId(Long cartId);

    Flux<Item> findAllBy(Pageable pageable);

    @Modifying
    @Query("UPDATE items SET count = 0")
    Mono<Void> resetAllCounts();

    @Modifying
    @Query("UPDATE items SET cart_id = null")
    Mono<Void> resetAllCartId();
}
