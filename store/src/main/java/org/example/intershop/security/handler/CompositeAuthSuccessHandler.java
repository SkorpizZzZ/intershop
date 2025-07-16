package org.example.intershop.security.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CompositeAuthSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final CartItemsLoginHandler cartItemsLoginHandler;
    private final RedirectServerAuthenticationSuccessHandler redirectSuccessHandler;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return cartItemsLoginHandler.onAuthenticationSuccess(webFilterExchange, authentication)
                .then(Mono.defer(() -> redirectSuccessHandler.onAuthenticationSuccess(webFilterExchange, authentication)));
    }
}
