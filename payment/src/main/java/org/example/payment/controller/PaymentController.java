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
    public Mono<ResponseEntity<BigDecimal>> balanceGet(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(balance -> ResponseEntity.ok()
                        .body(balance)
                );
    }

    @Override
    public Mono<ResponseEntity<BigDecimal>> balancePost(Mono<BigDecimal> body, ServerWebExchange exchange) {
        return null;
    }
}
