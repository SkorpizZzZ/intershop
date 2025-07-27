package org.example.intershop.repository;

import org.example.intershop.domain.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, String> {
    Mono<User> findByUsername(String username);

    Mono<User> save(User userDto);

    Mono<Long> getIdByUsername(String username);
}
