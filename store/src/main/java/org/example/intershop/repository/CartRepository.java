package org.example.intershop.repository;

import org.example.intershop.domain.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CartRepository extends R2dbcRepository<Cart, Long> {

    Mono<Cart> findByUsername(String username);
}
