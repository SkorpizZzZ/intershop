package org.example.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.controller.DefaultApi;
import org.example.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BigDecimal>> balanceUsernameGet(String username, ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .flatMap(principal -> {
                    if (principal instanceof JwtAuthenticationToken jwtAuth) {
                        String tokenUsername = jwtAuth.getToken().getClaimAsString("username");
                        log.debug("JWT claims: {}", jwtAuth.getToken().getClaims());

                        if (!username.equals(tokenUsername)) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Username in path does not match token username"
                            ));
                        }

                        return paymentService.getBalance(username);
                    }
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid authentication token"
                    ));
                })
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<BigDecimal>> balanceUsernamePost(String username, Mono<BigDecimal> amount, ServerWebExchange exchange) {
        return paymentService.pay(amount, username)
                        .map(response -> ResponseEntity.ok()
                                .body(response));
    }
}
