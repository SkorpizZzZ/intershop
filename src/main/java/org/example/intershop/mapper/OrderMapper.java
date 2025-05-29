package org.example.intershop.mapper;

import org.example.intershop.domain.Order;
import org.example.intershop.dto.OrderDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDto orderEntityToOrderDto(Order order);
}
