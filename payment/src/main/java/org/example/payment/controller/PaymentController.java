package org.example.payment.controller;

import lombok.RequiredArgsConstructor;
import org.example.controller.DefaultApi;
import org.example.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BigDecimal>> balanceUsernameGet(String username, ServerWebExchange exchange) {
        return paymentService.getBalance(username)
                        .map(balance -> ResponseEntity.ok()
                                .body(balance));
    }

    @Override
    public Mono<ResponseEntity<BigDecimal>> balanceUsernamePost(String username, Mono<BigDecimal> amount, ServerWebExchange exchange) {
        return paymentService.pay(amount, username)
                        .map(response -> ResponseEntity.ok()
                                .body(response));
    }
}
