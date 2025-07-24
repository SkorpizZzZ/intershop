package org.example.intershop.security.handler;

import lombok.RequiredArgsConstructor;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.service.CartService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CartLogoutHandler implements ServerLogoutHandler {

    private final ItemRepository itemRepository;
    private final CartService cartService;

    @Override
    @CacheEvict(value = "items", allEntries = true)
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        return cartService.getCartByUsername(authentication.getName())
                .flatMap(cart -> cartService.deleteAllByCartId(cart.id())
                        .then(cartService.saveCartItems(cart).then())
                        .then(itemRepository.resetAllCountsAndCartIds())
                );
    }
}
