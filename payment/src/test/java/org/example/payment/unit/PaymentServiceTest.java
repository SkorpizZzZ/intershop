package org.example.payment.unit;

import org.example.payment.domain.AccountEntity;
import org.example.payment.exception.NotEnoughMoneyException;
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
import static org.mockito.ArgumentMatchers.anyString;
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
        String inputName = "foo";

        @Test
        @DisplayName("Новый юзер")
        void newUser() {
            //GIVEN
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setUsername(inputName);
            accountEntity.setBalance(new BigDecimal("100000.00"));
            Mono<AccountEntity> newAcc = Mono.just(accountEntity);
            //WHEN
            when(repository.getBalanceByUsername(anyString())).thenReturn(Mono.empty());
            when(repository.save(any())).thenReturn(newAcc);
            //THEN
            Mono<BigDecimal> actualResult = service.getBalance(inputName);
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isEqualTo(new BigDecimal("100000.00")))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Аккаунт уже существует")
        void accountAlreadyExists() {
            //WHEN
            when(repository.getBalanceByUsername(anyString())).thenReturn(Mono.just(BigDecimal.ONE));
            //THEN
            Mono<BigDecimal> actualResult = service.getBalance(inputName);
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isEqualTo(BigDecimal.ONE))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Выполнение платежа")
    class Pay {
        Mono<BigDecimal> inputAmount = Mono.just(BigDecimal.TWO);
        String inputName = "foo";

        @Test
        @DisplayName("Платеж выполнился успешно")
        void happyPath() {
            //GIVEN
            BigDecimal expectedResult = new BigDecimal("8.00");
            //WHEN
            when(repository.getBalanceByUsername(anyString()))
                    .thenReturn(Mono.just(BigDecimal.TEN))
                    .thenReturn(Mono.just(new BigDecimal("8.00")));
            when(repository.withdraw(any(BigDecimal.class), anyString())).thenReturn(Mono.empty());
            //THEN
            Mono<BigDecimal> actualResult = service.pay(inputAmount, inputName);
            StepVerifier.create(actualResult)
                    .assertNext(result -> assertThat(result).isEqualTo(expectedResult))
                    .verifyComplete();
        }

        @Test
        @DisplayName("На счете недостаточно средств")
        void notEnoughMoney() {
            //WHEN
            when(repository.getBalanceByUsername(anyString())).thenReturn(Mono.just(BigDecimal.ONE));
            //THEN
            Mono<BigDecimal> actualResult = service.pay(inputAmount, inputName);
            StepVerifier.create(actualResult)
                    .expectErrorMatches(throwable ->
                            throwable instanceof NotEnoughMoneyException &&
                            throwable.getMessage().equals("Недостаточно средств на балансе. Сумма баланса 1")
                    ).verify();
        }
    }
}
