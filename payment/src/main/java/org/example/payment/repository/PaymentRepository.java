package org.example.payment.repository;

import org.example.payment.domain.AccountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentRepository extends R2dbcRepository<AccountEntity, Long> {

    @Query("SELECT ac.balance FROM accounts AS ac WHERE ac.id = 1")
    Mono<BigDecimal> getCurrentBalance();
}
