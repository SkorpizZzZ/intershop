package org.example.payment.integration.service;

import org.example.payment.domain.AccountEntity;
import org.example.payment.exception.NotEnoughMoneyException;
import org.example.payment.integration.AbstractIntegration;
import org.example.payment.repository.AccountRepository;
import org.example.payment.service.PaymentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentServiceIT extends AbstractIntegration {

    @Autowired
    private PaymentService service;
    @Autowired
    private AccountRepository repository;

    private final String USERNAME = "foo";

    @BeforeAll
    void saveNewAcc() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(USERNAME);
        accountEntity.setBalance(new BigDecimal("100000.00"));
        repository.save(accountEntity).block();
    }

    @BeforeEach
    void setUp() {
        repository.updateBalance(new BigDecimal("100000.00"), USERNAME)
                .block();
    }

    @Nested
    @DisplayName("Процесс платежа")
    class Pay {
        @Test
        @DisplayName("Успешный платеж")
        void happyPath() {
            BigDecimal inputParam = new BigDecimal("80000.00");
            BigDecimal expectedResult = new BigDecimal("20000.00");
            //THEN
            Mono<BigDecimal> actualResult = service.pay(Mono.just(inputParam), USERNAME);
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
            Mono<BigDecimal> actualResult = service.pay(Mono.just(inputParam), USERNAME);
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof NotEnoughMoneyException &&
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
            Mono<BigDecimal> actualResult = service.getBalance(USERNAME);
            StepVerifier.create(actualResult)
                    .assertNext(balance -> assertThat(balance).isEqualTo(expectedResult))
                    .verifyComplete();
        }
    }
}
