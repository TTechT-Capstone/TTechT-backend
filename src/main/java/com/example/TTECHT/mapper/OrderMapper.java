package com.example.TTECHT.mapper;


import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.entity.order.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(OrderCreationRequest request);

    OrderResponse toOrderResponse(Order order);

}
