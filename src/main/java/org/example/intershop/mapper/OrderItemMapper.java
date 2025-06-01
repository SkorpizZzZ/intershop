package org.example.intershop.mapper;

import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderItemDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemDto orderItemToOrderItemDto(OrderItem orderItem);
}
