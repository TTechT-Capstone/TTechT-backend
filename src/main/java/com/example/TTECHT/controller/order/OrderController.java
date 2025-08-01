package com.example.TTECHT.controller.order;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.dto.request.UpdateOrderStatusRequest;
import com.example.TTECHT.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping("/{userId}/{cartId}")
    ApiResponse<OrderResponse> createOrder(@PathVariable Long userId, @PathVariable Long cartId, @RequestBody @Valid OrderCreationRequest orderCreationRequest) {
        // Logic to create a new order
        log.info("Create order for user ID: {}, cart ID: {}", userId, cartId);
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(userId, cartId, orderCreationRequest))
                .build();
    }

    @GetMapping("/{orderId}")
    ApiResponse<OrderResponse> getOrder(@PathVariable Long orderId) {
        // Logic to retrieve an order by its ID
        log.info("Get order with ID: {}", orderId);
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrder(orderId))
                .build();
    }

    @GetMapping("/user/{userId}")
    ApiResponse<List<OrderResponse>> getOrderByUserId(@PathVariable Long userId) {
        // Logic to retrieve an order by user ID
        log.info("Get order for user ID: {}", userId);
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getOrderByUserId(userId))
                .build();
    }

    @PutMapping("/{orderId}")
    ApiResponse<OrderResponse> updateOrder(@PathVariable Long orderId, @RequestBody @Valid OrderCreationRequest orderCreationRequest) {
        log.info("Update order with ID: {}", orderId);
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrder(orderId, orderCreationRequest))
                .build();
    }
    
    @PutMapping("/{orderId}/status")
    ApiResponse<Void> updateOrderStatus(@PathVariable Long orderId, @RequestBody @Valid UpdateOrderStatusRequest updateOrderStatusRequest) {
        log.info("Update order status with ID: {}", orderId);
        System.out.println("dm" + updateOrderStatusRequest.toString());
        orderService.updateOrderStatus(orderId, updateOrderStatusRequest);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    ApiResponse<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancel order with ID: {}", orderId);
        orderService.cancelOrder(orderId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/all")
    ApiResponse<List<OrderResponse>> getAllOrders() {
        log.info("Get all orders");
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAllOrders())
                .build();
    }
}
