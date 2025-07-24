package org.example.intershop.repository;

import org.example.intershop.domain.CartItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {

    Flux<CartItem> findAllByCartId(Long cartId);

    Mono<Void> deleteAllByCartId(Long cartId);
}
