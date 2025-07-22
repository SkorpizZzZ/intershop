package org.example.payment.unit;

import org.example.payment.repository.AccountRepository;
import org.example.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService service;

    @Mock
    private AccountRepository repository;

    @Nested
    @DisplayName("Получение текущего баланса")
    class GetBalance {
        @Test
        void getBalance() {
            //WHEN
            when(repository.getCurrentBalance()).thenReturn(Mono.just(BigDecimal.ONE));
            //THEN
            Mono<BigDecimal> actualResult = service.getBalance();
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isEqualTo(BigDecimal.ONE))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Выполнение платежа")
    class Pay {
        Mono<BigDecimal> inputAmount = Mono.just(BigDecimal.TWO);

        @Test
        @DisplayName("Платеж выполнился успешно")
        void happyPath() {
            //GIVEN
            BigDecimal expectedResult = new BigDecimal("8.00");
            //WHEN
            when(repository.getCurrentBalance()).thenReturn(Mono.just(BigDecimal.TEN));
            when(repository.withdraw(any(BigDecimal.class))).thenReturn(Mono.just(new BigDecimal("8.00")));
            //THEN
            Mono<BigDecimal> actualResult = service.pay(inputAmount);
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isEqualTo(expectedResult))
                    .verifyComplete();
        }

        @Test
        @DisplayName("На счете недостаточно средств")
        void notEnoughMoney() {
            //WHEN
            when(repository.getCurrentBalance()).thenReturn(Mono.just(BigDecimal.ONE));
            //THEN
            Mono<BigDecimal> actualResult = service.pay(inputAmount);
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof IllegalArgumentException &&
                            throwable.getMessage().equals("Недостаточно средств на балансе. Сумма баланса 1")
                    ).verify();
        }
    }
}
