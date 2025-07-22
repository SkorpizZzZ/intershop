package org.example.payment.integration.service;

import org.example.payment.integration.AbstractIntegration;
import org.example.payment.repository.AccountRepository;
import org.example.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentServiceIT extends AbstractIntegration {

    @Autowired
    private PaymentService service;
    @Autowired
    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        repository.updateBalance(new BigDecimal("100000.00"))
                .block();
    }

    @Nested
    @DisplayName("Процесс платежа")
    class Pay {
        @Test
        @DisplayName("Успешный платеж")
        void happyPath() {
            BigDecimal inputParam = new BigDecimal("80000");
            BigDecimal expectedResult = new BigDecimal("20000.00");
            //THEN
            Mono<BigDecimal> actualResult = service.pay(Mono.just(inputParam));
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isEqualTo(expectedResult))
                    .verifyComplete();
        }

        @Test
        @DisplayName("На балансе не достаточно средств")
        void notEnoughMoney() {
            //GIVEN
            BigDecimal inputParam = new BigDecimal("20000000");
            //THEN
            Mono<BigDecimal> actualResult = service.pay(Mono.just(inputParam));
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof IllegalArgumentException &&
                            throwable.getMessage().equals("Недостаточно средств на балансе. Сумма баланса 100000.00")
                    ).verify();
        }
    }


    @Nested
    @DisplayName("Получение текущего баланса")
    class GetBalance {
        @Test
        @DisplayName("Успешное получение текущего баланса")
        void getBalance() {
            //GIVEN
            BigDecimal expectedResult = new BigDecimal("100000.00");
            //THEN
            Mono<BigDecimal> actualResult = service.getBalance();
            StepVerifier.create(actualResult)
                    .assertNext(balance -> assertThat(balance).isEqualTo(expectedResult))
                    .verifyComplete();
        }
    }
}
