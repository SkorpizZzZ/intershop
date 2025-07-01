package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.payment.exception.NotEnoughMoneyException;
import org.example.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Mono<BigDecimal> getBalance() {
        return paymentRepository.getCurrentBalance();
    }

    @Transactional
    public Mono<BigDecimal> pay(Mono<BigDecimal> amount) {
        return getBalance()
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
                    return paymentRepository.withdraw(payAmount);
                });
    }
}
