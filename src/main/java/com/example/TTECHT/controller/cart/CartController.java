package com.example.TTECHT.controller.cart;

import com.example.TTECHT.dto.repsonse.CartResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.CartCreationRequest;
import com.example.TTECHT.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartController {

    CartService cartService;
    // This class will handle requests related to the shopping cart.
    // It will interact with the CartService and CartItemService to manage cart operations.
    @PostMapping("/{userId}")
    ApiResponse<CartResponse> createCart(@PathVariable String userId, @RequestBody @Valid CartCreationRequest request) {
        // Logic to create a new cart
        log.info("Create cart: {}", request);
        return ApiResponse.<CartResponse>builder()
                .result(cartService.createCart(userId, request))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<CartResponse> getCart(@PathVariable String userId) {
        // Logic to retrieve the cart for a specific user
        log.info("Get cart for user: {}", userId);
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getCart(userId))
                .build();
    }

    @DeleteMapping("/{userId}/{cartId}")
    ApiResponse<String> deleteCart(@PathVariable String userId, @PathVariable String cartId) {
        // Logic to delete the cart for a specific user
        log.info("Delete cart for user: {}", userId);
        cartService.deleteCart(userId, cartId);
        return ApiResponse.<String>builder()
                .result("Cart deleted successfully")
                .build();
    }

    @PostMapping("/submit/{userId}/{cartId}")
    ApiResponse<String> submitCart(@PathVariable String userId, @PathVariable String cartId) {
        // Assuming there's a method in CartService to handle cart submission
        cartService.submitCart(userId, cartId);
        return ApiResponse.<String>builder()
                .result("Cart submitted successfully")
                .build();
    }

}
