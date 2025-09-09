package com.example.TTECHT.mapper;


import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.repsonse.OrderItemReponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.entity.order.Order;
import com.example.TTECHT.entity.order.OrderItem;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "contactName", ignore = true)
    @Mapping(target = "contactEmail", ignore = true)
    @Mapping(target = "contactPhone", ignore = true)
    @Mapping(target = "deliveryAddress", ignore = true)
    @Mapping(target = "promotionCode", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    Order toOrder(OrderCreationRequest request);

    @Mapping(target = "id", source = "orderItemId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "discountPrice", source = "discountPrice")
    @Mapping(target = "stockCode", source = "stockCode")
    @Mapping(target = "selectedColor", source = "selectedColor")
    @Mapping(target = "selectedSize", source = "selectedSize")
    @Mapping(target = "createdBy", expression = "java(orderItem.getCreatedAt() != null ? orderItem.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedBy", expression = "java(orderItem.getUpdatedAt() != null ? orderItem.getUpdatedAt().toString() : null)")
    OrderItemReponse toOrderItemResponse(OrderItem orderItem);

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
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "cancellationReason", source = "cancellationReason")
    @Mapping(target = "cancelledAt", expression = "java(order.getCancelledAt() != null ? order.getCancelledAt().toString() : null)")
    @Mapping(target = "cancelledBy", source = "cancelledBy")
    OrderResponse toOrderResponse(Order order);

}
