package org.example.payment.integration.service;

import org.example.payment.integration.AbstractIntegration;
import org.example.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentServiceTest extends AbstractIntegration {

    @Autowired
    private PaymentService service;

    @Test
    @DisplayName("Получение текущего баланса")
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
