package org.example.intershop.security.handler;

import lombok.RequiredArgsConstructor;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.service.CartItemService;
import org.example.intershop.service.CartService;
import org.example.intershop.service.ItemService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CartItemsLoginHandler implements ServerAuthenticationSuccessHandler {

    private final CartService cartService;
    private final ItemService itemService;
    private final CartItemService cartItemService;

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return cartService.getCartByUsername(authentication.getName())
                .flatMapMany(cartDto -> cartItemService.findAllByCartId(cartDto.id())
                        .flatMap(cartItemDto -> itemRepository.findById(cartItemDto.itemId())
                                .map(item -> itemMapper.cartItemDtoToItem(cartItemDto, item))
                                .flatMap(item -> itemService.updateItem(item, cartDto.id(), "current"))))
                .then();
    }
}
