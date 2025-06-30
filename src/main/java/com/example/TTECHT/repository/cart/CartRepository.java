package com.example.TTECHT.repository.cart;

import com.example.TTECHT.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long > {
    // This class will handle the database operations related to Cart
    // For example, creating a new cart, retrieving a cart by user ID, updating a cart, etc.
    // It will interact with the database using JPA or any other ORM framework.

    // Methods to be implemented:
    // - createCart(CreateCartRequest request)
    // - getCartByUserId(Long userId)
    // - updateCart(CartResponse cartResponse)
    // - deleteCart(Long cartId)

    Optional<Cart> findByUserId(Long userId);
    void deleteByUserId(Long userId);

}
