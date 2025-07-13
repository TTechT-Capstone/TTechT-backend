package com.example.TTECHT.controller.cart;


import com.example.TTECHT.dto.repsonse.CartItemResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.CartItemRequest;
import com.example.TTECHT.service.CartItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cartsItems")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartItemController {

    CartItemService cartItemService;

    @PostMapping
    ApiResponse<CartItemResponse> addItemToCart(@RequestBody @Valid CartItemRequest request) {
        System.out.println("Adding item to cart: " + request.getCartId() + request.getProductId() + request.getQuantity());
        return ApiResponse.<CartItemResponse>builder()
                .result(cartItemService.addItemToCart(request))
                .build();
    }

    @PostMapping("/remove/{cartId}/{itemId}")
    ApiResponse<String> removeItemFromCart(@PathVariable Long cartId , @PathVariable Long itemId) {
        // Logic to remove an item from the cart
        log.info("Removing item with ID {} from cart with ID {}", itemId, cartId);
        System.out.println("DMMMMM");
        cartItemService.removeItemFromCart(cartId, itemId);
        return ApiResponse.<String>builder()
                .result("Item removed from cart successfully")
                .build();
    }


    @PutMapping("/quantity/{cartId}/{itemId}")
    ApiResponse<String> updateItemQuantity(@Param("newQuantity") int newQuantity, @PathVariable Long cartId, @PathVariable String itemId) {
        // Logic to update the quantity of an item in the cart
        log.info("Updating item with ID {} to new quantity {}", itemId, newQuantity);
        cartItemService.updateItemQuantity(cartId,Long.valueOf(itemId), newQuantity);
        return ApiResponse.<String>builder()
                .result("Item quantity updated successfully")
                .build();
    }


}
