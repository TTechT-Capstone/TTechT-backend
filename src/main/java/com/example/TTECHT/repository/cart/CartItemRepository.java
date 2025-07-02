package com.example.TTECHT.repository.cart;

import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // This class will handle the database operations related to Cart Items
    // For example, adding items to the cart, removing items, updating quantities, etc.
    // It will interact with the database using JPA or any other ORM framework.

    // Methods to be implemented:
    // - addItemToCart(CartItem item)
    // - removeItemFromCart(Long itemId)
    // - updateItemQuantity(Long itemId, int quantity)
    // - getCartItemsByCartId(Long cartId)

    void deleteByCartId(Long cartId);
    Boolean findByCartIdAndProduct(Long cart_id, Product product);
    List<CartItem> findByCartId(Long cartId);

}
