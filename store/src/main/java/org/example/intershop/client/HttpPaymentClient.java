package org.example.intershop.client;

import org.example.intershop.dto.HealthStatus;
import org.example.intershop.exception.PaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class HttpPaymentClient {
    private final WebClient client = WebClient.create();


    private final String paymentPort;
    private final String paymentHost;
    private final String basePath;
    private final String paymentUrl;

    public HttpPaymentClient(
            @Value("${submodule.server.payment.port}") String paymentPort,
            @Value("${submodule.server.payment.host}") String paymentHost,
            @Value("${spring.webflux.base-path}") String basePath
    ) {
        this.paymentPort = paymentPort;
        this.paymentHost = paymentHost;
        this.basePath = basePath;
        this.paymentUrl = String.format("http://%s:%s/%s/payment/balance",
                paymentHost, paymentPort, basePath);
    }

    public Mono<BigDecimal> pay(BigDecimal amount) {
        return client.post()
                .uri(paymentUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amount)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handlePaymentError)
                .bodyToMono(BigDecimal.class);
    }

    private Mono<PaymentException> handlePaymentError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .map(PaymentException::new)
                .flatMap(Mono::error);
    }

    public Mono<BigDecimal> getBalance() {
        return client.get()
                .uri(paymentUrl)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)));
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