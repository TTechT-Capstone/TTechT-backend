package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.CartItemResponse;
import com.example.TTECHT.dto.request.CartItemRequest;
import com.example.TTECHT.dto.request.CartItemUpdateRequest;
import com.example.TTECHT.entity.cart.CartItem;

import java.util.List;

public interface CartItemService {

    CartItemResponse addItemToCart(CartItemRequest request);
    CartItem removeItemFromCart(Long cartId, Long itemId);
    CartItem updateItemQuantity(Long cartId, Long itemId, int quantity);
    CartItemResponse updateCartItem(Long cartId, Long itemId, CartItemUpdateRequest request);
    CartItem getCartItemById(Long cartId, Long itemId);
    List<CartItem> getCartItems(Long cartId);

}
