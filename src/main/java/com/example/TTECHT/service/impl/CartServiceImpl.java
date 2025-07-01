package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.repsonse.CartResponse;
import com.example.TTECHT.dto.request.CartCreationRequest;
import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.exception.AppException;
import com.example.TTECHT.exception.ErrorCode;
import com.example.TTECHT.mapper.CartMapper;
import com.example.TTECHT.repository.cart.CartRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    UserRepository userRepository;
    CartMapper cartMapper;

    @PreAuthorize("hasRole('USER')")
    public CartResponse createCart(String userId, CartCreationRequest request) {

        // check if the userId is valid
        if (userId == null || userId.isEmpty()) {
            log.error("Invalid userId: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // check if this user exists in the database
        if (!userRepository.existsById(userId)) {
            log.error("User with ID {} does not exist", userId);
            throw new IllegalArgumentException("User does not exist");
        }

        // create a new cart
        Cart cart = cartMapper.toCart(request);
        cart.setUser(userRepository.findById(userId).orElseThrow());

        // save the cart to the database
        cart = cartRepository.save(cart);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .promotionCode(cart.getPromotionCode())
                .submittedTime(cart.getSubmittedTime())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    public CartResponse getCart(String userId) {
        // check if the userId is valid
        if (userId == null || userId.isEmpty()) {
            log.error("Invalid userId: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // check if this user exists in the database
        if (!userRepository.existsById(userId)) {
            log.error("User with ID {} does not exist", userId);
            throw new IllegalArgumentException("User does not exist");
        }

        // retrieve the cart for the user
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toCartResponse(cart);
    }

    @PreAuthorize("hasRole('USER')")
    public void deleteCart(String userId) {
        // check if the userId is valid
        if (userId == null || userId.isEmpty()) {
            log.error("Invalid userId: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // delete the cart for the user
        cartRepository.deleteByUserId(Long.valueOf(userId));
    }

    @PreAuthorize("hasRole('USER')")
    public void clearCart(String userId) {
        // check if the userId is valid
        if (userId == null || userId.isEmpty()) {
            log.error("Invalid userId: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // clear all items in the cart for the user
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

//        cart.getItems().clear(); // Assuming Cart has a method to clear items
        Cart cart = getCart(userId);
        cart.getItems().clear(); // Assuming Cart has a method to clear items
        cartRepository.save(cart);
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public void applyPromotionCode(String userId, String promotionCode) {
        // check if the userId is valid
        if (userId == null || userId.isEmpty()) {
            log.error("Invalid userId: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // retrieve the cart for the user
        Cart cart = cartRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        // apply the promotion code
        cart.setPromotionCode(promotionCode);
        cartRepository.save(cart);

    }

}
