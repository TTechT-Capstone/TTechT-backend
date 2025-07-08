package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.entity.order.Order;

public interface OrderService {
    OrderResponse createOrder(Long userId, Long cartId, OrderCreationRequest orderCreationRequest);
    OrderResponse getOrder(Long orderId);
    OrderResponse getOrderByUserId(Long userId);
    OrderResponse updateOrder(Long orderId, OrderCreationRequest orderCreationRequest);
}
