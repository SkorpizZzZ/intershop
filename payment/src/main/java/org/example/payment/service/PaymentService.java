package org.example.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.payment.domain.AccountEntity;
import org.example.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Mono<BigDecimal> getBalance() {
        return paymentRepository.getCurrentBalance();
    }

    public Flux<AccountEntity> findAll() {
        return paymentRepository.findAll();
    }
}
