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

    public Mono<BigDecimal> getBalance(String username) {
        return accountRepository.getBalanceByUsername(username)
                .switchIfEmpty(Mono.defer(() -> createAccount(username)
                        .map(Account::getBalance)));
    }

    private Mono<AccountEntity> createAccount(String username) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(username);
        accountEntity.setBalance(new BigDecimal("100000.00"));
        return accountRepository.save(accountEntity);
    }

    @Transactional
    public Mono<BigDecimal> pay(Mono<BigDecimal> amount, String username) {
        return accountRepository.getBalanceByUsername(username)
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
                    return accountRepository.withdraw(payAmount, username)
                            .then(accountRepository.getBalanceByUsername(username));
                });
    }
}
