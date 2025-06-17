package org.example.intershop.repository;

import org.example.intershop.domain.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CartRepository extends R2dbcRepository<Cart, Long> {
}
