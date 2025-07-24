package org.example.intershop.security.configuration;

import org.example.intershop.security.handler.CartItemsLoginHandler;
import org.example.intershop.security.handler.CompositeAuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;

@Configuration
public class AuthHandlersConfig {
    @Bean
    public RedirectServerAuthenticationSuccessHandler redirectSuccessHandler() {
        return new RedirectServerAuthenticationSuccessHandler("/main");
    }

    @Bean
    public CompositeAuthSuccessHandler compositeAuthSuccessHandler(
            CartItemsLoginHandler cartItemsLoginHandler,
            RedirectServerAuthenticationSuccessHandler redirectSuccessHandler
    ) {
        return new CompositeAuthSuccessHandler(cartItemsLoginHandler, redirectSuccessHandler);
    }
}
