package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;

public interface OrderService {
    OrderResponse createOrder(Long userId, Long cartId, OrderCreationRequest orderCreationRequest);
    OrderResponse getOrder(Long orderId);
    OrderResponse updateOrder(Long orderId, OrderCreationRequest orderCreationRequest);
}
