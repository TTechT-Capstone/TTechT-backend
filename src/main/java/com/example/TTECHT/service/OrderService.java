package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.enumuration.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId, Long cartId, OrderCreationRequest orderCreationRequest);
    OrderResponse getOrder(Long orderId);
    List<OrderResponse> getOrderByUserId(Long userId);
    OrderResponse updateOrder(Long orderId, OrderCreationRequest orderCreationRequest);
    void updateOrderStatus(Long orderId, OrderStatus orderStatus);
    void cancelOrder(Long orderId);
}
