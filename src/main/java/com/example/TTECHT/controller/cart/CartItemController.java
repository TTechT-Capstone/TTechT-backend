package com.example.TTECHT.controller.cart;


import com.example.TTECHT.dto.repsonse.CartItemResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.CartItemRequest;
import com.example.TTECHT.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cartsItems")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartItemController {

    CartItemService cartItemService;

    @PostMapping
    ApiResponse<CartItemResponse> addItemToCart(@RequestBody @Valid CartItemRequest request) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartItemService.addItemToCart(request))
                .build();
    }

    // Example method to remove an item from the cart
    public void removeItemFromCart(Long itemId) {
        // Logic to remove item from the cart
    }

    // Example method to update the quantity of an item in the cart
    public void updateItemQuantity(Long itemId, int newQuantity) {
        // Logic to update item quantity in the cart
    }

}
