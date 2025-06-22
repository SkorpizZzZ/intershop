package org.example.intershop.service.integration.service;

import org.example.intershop.client.HttpPaymentClient;
import org.example.intershop.service.integration.AbstractIntegration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpPaymentClientIT extends AbstractIntegration {

    @Autowired
    private HttpPaymentClient client;

    @Test
    void checkPaymentHealth() {
        Mono<Boolean> status = client.isPaymentServiceUp();
        StepVerifier.create(status)
                .assertNext(Assertions::assertTrue)
                .verifyComplete();
    }

    @Test
    void check() {
        //GIVEN
        BigDecimal expectedResult = new BigDecimal("100000.00");

        //THEN
        Mono<BigDecimal> actualResult = client.getBalance();
        StepVerifier.create(actualResult)
                .assertNext(balance -> assertThat(balance).isEqualTo(expectedResult))
                .verifyComplete();
    }
}
