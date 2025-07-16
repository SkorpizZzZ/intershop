package org.example.intershop.service;

import lombok.RequiredArgsConstructor;
import org.example.intershop.dto.CartItemDto;
import org.example.intershop.mapper.CartItemMapper;
import org.example.intershop.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    @Transactional(readOnly = true)
    public Flux<CartItemDto> findAllByCartId(Long cartId) {
        return cartItemRepository.findAllByCartId(cartId)
                .map(cartItemMapper::cartItemToCartItemDto);
    }

}
