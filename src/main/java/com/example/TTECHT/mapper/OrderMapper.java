package com.example.TTECHT.mapper;


import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.entity.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "contactName", ignore = true)
    @Mapping(target = "contactEmail", ignore = true)
    @Mapping(target = "contactPhone", ignore = true)
    @Mapping(target = "deliveryAddress", ignore = true)
    @Mapping(target = "promotionCode", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Order toOrder(OrderCreationRequest request);



    @Mapping(target = "id", source = "orderId")
    @Mapping(target = "orderNumber", source = "orderNumber")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "orderStatus", source = "orderStatus")
    @Mapping(target = "contactName", source = "contactName")
    @Mapping(target = "contactEmail", source = "contactEmail")
    @Mapping(target = "contactPhone", source = "contactPhone")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    @Mapping(target = "promotionCode", source = "promotionCode")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    OrderResponse toOrderResponse(Order order);

}
