package org.example.intershop.mapper;

import org.example.intershop.domain.Order;
import org.example.intershop.domain.OrderItem;
import org.example.intershop.dto.OrderDto;
import org.example.intershop.dto.OrderItemDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    ItemMapper itemMapper = new ItemMapperImpl();

//    @Mapping(target = "orderItems", expression = "java(mapOrderItems(order.getOrderItems()))")
    OrderDto orderEntityToOrderDto(Order order);

    default List<OrderItemDto> mapOrderItems(List<OrderItem> items) {
        return null;
//        List<OrderItemDto> result = new ArrayList<>(items.size());
//        for (OrderItem orderItem : items) {
//            result.add(new OrderItemDto(
//                    orderItem.getId(),
//                    itemMapper.itemEntityToItemDto(orderItem.getId()),
//                    orderItem.getQuantity()
//            ));
//        }
//        return result;
    }

}
