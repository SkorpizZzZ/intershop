package org.example.intershop.mapper;

import org.example.intershop.domain.Item;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", source = "orderItem.id")
    OrderItemDto orderItemToOrderItemDto(OrderItem orderItem, Item item);
}
