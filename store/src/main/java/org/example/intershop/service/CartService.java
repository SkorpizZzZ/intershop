package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.example.intershop.domain.CartItem;
import org.example.intershop.dto.CartDto;
import org.example.intershop.dto.CartItemDto;
import org.example.intershop.mapper.CartItemMapper;
import org.example.intershop.mapper.ItemMapper;
import org.example.intershop.repository.CartItemRepository;
import org.example.intershop.repository.CartRepository;
import org.example.intershop.repository.ItemRepository;
import org.example.intershop.security.service.SecurityService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    private final ItemMapper itemMapper;
    private final CartItemMapper cartItemMapper;

    private final SecurityService securityService;

    @Transactional(readOnly = true)
    @Cacheable(value = "items")
    public Mono<CartDto> getCart() {
        return securityService.getUsername()
                .flatMap(this::getCartByUsername);
    }

    @Transactional
    public Flux<CartItemDto> saveCartItems(CartDto cart) {
        List<CartItem> cartItems = cartItemMapper.itemDtosToCartItems(cart.items());
        return cartItemRepository.saveAll(cartItems)
                .map(cartItemMapper::cartItemToCartItemDto);
    }

    @Transactional(readOnly = true)
    public Mono<CartDto> getCartByUsername(String username) {
        return cartRepository.findByUsername(username)
                .flatMap(cart -> itemRepository.findAllByCartId(cart.getId())
                        .map(itemMapper::itemEntityToItemDto).collectList()
                        .map(items -> new CartDto(cart.getId(), cart.getUsername(), items)));
    }

    public Mono<Void> deleteAllByCartId(Long cartId) {
        return cartItemRepository.deleteAllByCartId(cartId);
    }
}
