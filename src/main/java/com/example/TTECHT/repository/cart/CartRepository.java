package com.example.TTECHT.repository.cart;

import com.example.TTECHT.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long > {

    // Methods to be implemented:
    // - createCart(CreateCartRequest request)
    // - getCartByUserId(Long userId)
    // - updateCart(CartResponse cartResponse)
    // - deleteCart(Long cartId)

    Optional<Cart> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    void deleteById(Long cartId);
}
