package org.example.intershop.client;

import org.example.intershop.dto.HealthStatus;
import org.example.intershop.exception.PaymentException;
import org.example.intershop.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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
    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final SecurityService securityService;


    private final String paymentPort;
    private final String paymentHost;
    private final String basePath;
    private final String paymentUrl;
    private final String clientId;

    public HttpPaymentClient(
            ReactiveOAuth2AuthorizedClientManager manager, SecurityService securityService,
            @Value("${submodule.server.payment.port}") String paymentPort,
            @Value("${submodule.server.payment.host}") String paymentHost,
            @Value("${spring.webflux.base-path}") String basePath,
            @Value("${spring.security.oauth2.client.registration.yandex-store.client-id}") String clientId
    ) {
        this.manager = manager;
        this.securityService = securityService;
        this.paymentPort = paymentPort;
        this.paymentHost = paymentHost;
        this.basePath = basePath;
        this.clientId = clientId;
        this.paymentUrl = String.format("http://%s:%s/%s/payment",
                paymentHost, paymentPort, basePath);
    }

    public Mono<BigDecimal> pay(BigDecimal amount) {
        return securityService.getUsername()
                .flatMap(username -> getToken()
                        .flatMap(accessToken -> client.post()
                                .uri(paymentUrl + "/balance/{username}", username)
                                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(amount)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, this::handlePaymentError)
                                .bodyToMono(BigDecimal.class))
                );
    }

    private Mono<PaymentException> handlePaymentError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .map(PaymentException::new)
                .flatMap(Mono::error);
    }

    public Mono<BigDecimal> getBalance() {
        return securityService.getUsername()
                .flatMap(username -> getToken()
                        .flatMap(accessToken -> client.get()
                                .uri(paymentUrl + "/balance/{username}", username)
                                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                                .retrieve()
                                .bodyToMono(BigDecimal.class)
                                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)))
                        )
                );
    }

    public Mono<Boolean> isPaymentServiceUp() {
        String healthUrl = String.format("http://%s:%s/%s/actuator/health",
                paymentHost, paymentPort, basePath);
        return getToken()
                .flatMap(accessToken -> client.get()
                        .uri(healthUrl)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .retrieve()
                        .bodyToMono(HealthStatus.class)
                        .map(response -> "UP".equals(response.status()))
                        .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)))
                        .onErrorReturn(false)
                );
    }

    private Mono<String> getToken() {
        return securityService.getUsername()
                        .flatMap(username ->
                            manager.authorize(OAuth2AuthorizeRequest
                                            .withClientRegistrationId(clientId)
                                            .principal("system")
                                            .attribute("username", username)
                                            .attribute("preferred_username", username)
                                            .build())
                                    .map(OAuth2AuthorizedClient::getAccessToken)
                                    .map(OAuth2AccessToken::getTokenValue));
    }
}