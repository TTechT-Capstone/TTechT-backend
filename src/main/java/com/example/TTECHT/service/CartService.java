package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.CartResponse;
import com.example.TTECHT.dto.request.CartCreationRequest;

public interface CartService {
    CartResponse createCart(String userId, CartCreationRequest request);
    CartResponse getCart(String userId);
    void deleteCart(String userId, String cartId);
    // clear all items in the cart
    void clearCart(String userId, String cartId);
    void applyPromotionCode(String userId, String promotionCode);
}
