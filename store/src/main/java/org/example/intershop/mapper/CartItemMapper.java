package org.example.intershop.mapper;

import org.example.intershop.domain.CartItem;
import org.example.intershop.dto.CartItemDto;
import org.example.intershop.dto.ItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "quantity", source = "count")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", source = "id")
    CartItem itemDtoToCartItem(ItemDto itemDto);

    List<CartItem> itemDtosToCartItems(List<ItemDto> itemDtos);
    CartItemDto cartItemToCartItemDto(CartItem cartItem);
    List<CartItemDto> cartItemsToCartItemDtos(List<CartItem> cartItems);
}
