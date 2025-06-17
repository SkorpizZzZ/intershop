package org.example.intershop.mapper;

import org.example.intershop.domain.Item;
import org.example.intershop.dto.ItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "cartId", source = "cartId")
    ItemDto itemEntityToItemDto(Item item);
}
