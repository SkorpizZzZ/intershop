package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.Account;
import org.example.payment.domain.AccountEntity;
import org.example.payment.exception.NotEnoughMoneyException;
import org.example.payment.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;

    public Mono<BigDecimal> getBalance(Long userId) {
        return accountRepository.getBalanceByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> createAccount(userId)
                        .map(Account::getBalance)));
    }

    private Mono<AccountEntity> createAccount(Long userId) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUserId(userId);
        accountEntity.setBalance(new BigDecimal("100000.00"));
        return accountRepository.save(accountEntity);
    }

    @Transactional
    public Mono<BigDecimal> pay(Mono<BigDecimal> amount, Long userId) {
        return accountRepository.getBalanceByUserId(userId)
                .zipWith(amount)
                .flatMap(tuple -> {
                    BigDecimal currentBalance = tuple.getT1();
                    BigDecimal payAmount = tuple.getT2();
                    if (payAmount.compareTo(currentBalance) > 0) {
                        return Mono.error(
                                new NotEnoughMoneyException(
                                        String.format("Недостаточно средств на балансе. Сумма баланса %s", currentBalance)
                                )
                        );
                    }
                    return accountRepository.withdraw(payAmount, userId)
                            .then(accountRepository.getBalanceByUserId(userId));
                });
    }
}
