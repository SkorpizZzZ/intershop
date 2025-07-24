package org.example.intershop.mapper;

import org.example.intershop.domain.Item;
import org.example.intershop.dto.CartItemDto;
import org.example.intershop.dto.ItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "cartId", source = "cartId")
    ItemDto itemEntityToItemDto(Item item);

    List<Item> itemDtosToItems(List<ItemDto> itemDtos);

    @Mapping(target = ".", source = "item")
    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "count", source = "cartItemDto.quantity")
    @Mapping(target = "cartId", source = "cartItemDto.cartId")
    Item cartItemDtoToItem(CartItemDto cartItemDto, Item item);
}
