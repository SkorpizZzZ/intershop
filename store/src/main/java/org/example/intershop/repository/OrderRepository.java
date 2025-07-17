package org.example.intershop.repository;

import org.example.intershop.domain.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Flux<Order> findAllByCartId(Long cartId);

    Mono<Order> findByIdAndCartId(Long id, Long cartId);
}
