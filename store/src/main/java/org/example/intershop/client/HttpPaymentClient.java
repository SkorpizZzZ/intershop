package org.example.intershop.client;

import org.example.intershop.dto.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class HttpPaymentClient {
    private final WebClient client = WebClient.create();

    @Value("${submodule.server.payment.port}")
    private String paymentPort;
    @Value("${submodule.server.payment.host}")
    private String paymentHost;
    @Value("${spring.webflux.base-path}")
    private String basePath;

    public Mono<BigDecimal> getBalance() {
        String balanceUrl = String.format("http://%s:%s/%s/payment/balance",
                paymentHost, paymentPort, basePath);

        return client.get()
                .uri(balanceUrl)
                .retrieve()
                .bodyToMono(BigDecimal.class);
    }

    public Mono<Boolean> isPaymentServiceUp() {
        String healthUrl = String.format("http://%s:%s/%s/actuator/health",
                paymentHost, paymentPort, basePath);
        return client.get()
                .uri(healthUrl)
                .retrieve()
                .bodyToMono(HealthStatus.class)
                .map(response -> "UP".equals(response.status()))
                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)))
                .onErrorReturn(false);
    }
}