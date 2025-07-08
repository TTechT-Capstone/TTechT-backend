package com.example.TTECHT.controller.order;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
}
