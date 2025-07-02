package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.repsonse.CartItemResponse;
import com.example.TTECHT.dto.request.CartItemRequest;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.entity.cart.CartItem;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.cart.CartItemRepository;
import com.example.TTECHT.repository.cart.CartRepository;
import com.example.TTECHT.service.CartItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartItemServiceImpl implements CartItemService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;

    public CartItemResponse addItemToCart(CartItemRequest request) {

        Cart cart = cartRepository.findById(Long.valueOf(request.getCardId())).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + request.getCardId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

        // check if the product is already in the cart
       if (cartItemRepository.findByCartIdAndProduct(Long.valueOf(request.getCardId()), product) != null) {
            log.error("Product with ID {} is already in the cart with ID {}", request.getProductId(), request.getCardId());
            throw new IllegalArgumentException("Product is already in the cart");
        }

        // CartItem builder
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .productName(product.getName())
                .quantity(request.getQuantity())
                .build();

        cartItemRepository.save(cartItem);
        return CartItemResponse.builder()
                .id(cartItem.getCartItemId())
                .productId(product.getProductId())
                .quantity(cartItem.getQuantity())
                .build();
    }

    public List<CartItem> getCartItems(Long cartId) {
        cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        return cartItemRepository.findByCartId(cartId);
    }

    public CartItem removeItemFromCart(Long cartId, Long itemId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        cartItemRepository.delete(cartItem);
        return cartItem;
    }


    @Transactional
    public CartItem updateItemQuantity(Long cartId, Long itemId, int quantity) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public CartItem getCartItemById(Long cartId, Long itemId) {
        cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        return cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));
    }

}
