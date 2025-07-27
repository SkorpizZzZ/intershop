package org.example.payment.repository;

import org.example.payment.domain.AccountEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {


    @Query("""
            SELECT balance
            FROM accounts
            WHERE user_id = :userId
            """)
    Mono<BigDecimal> getBalanceByUserId(Long userId);

    @Modifying
    @Query("""
            UPDATE accounts
            SET balance = balance - :amount
            WHERE user_id = :userId
            """)
    Mono<Void> withdraw(BigDecimal amount, Long userId);

    @Modifying
    @Query("""
            UPDATE accounts
            SET balance = :amount
            WHERE user_id = :userId
            """)
    Mono<BigDecimal> updateBalance(BigDecimal amount, Long userId);

}
