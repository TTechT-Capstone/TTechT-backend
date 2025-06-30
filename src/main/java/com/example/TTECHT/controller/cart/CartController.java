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

    // Example method to add an item to the cart
    public void addItemToCart(Long itemId, int quantity) {
        // Logic to add item to cart
    }

    // Example method to remove an item from the cart
    public void removeItemFromCart(Long itemId) {
        // Logic to remove item from cart
    }

    // Example method to view the cart
    public void viewCart() {
        // Logic to view items in the cart
    }
}
