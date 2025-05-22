package org.example.intershop.mapper;

import org.example.intershop.domain.Item;
import org.example.intershop.dto.ItemDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemDto itemEntityToItemDto(Item item);
    Item itemDtoToItemEntity(ItemDto itemDto);
}
