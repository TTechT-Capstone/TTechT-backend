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
    Cart cart = cartRepository.findById(Long.valueOf(request.getCartId())) // Fixed: removed Long.valueOf if getCartId returns Long
            .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + request.getCartId()));

    Product product = productRepository.findById(request.getProductId()) // Fixed: use actual product ID
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

    //??? check lại
    // Check if the product is already in the cart
//    if (cartItemRepository.existsByCartIdAndProduct(request.getCartId(), product)) { // Fixed: use existsBy
//        log.error("Product with ID {} is already in the cart with ID {}", request.getProductId(), request.getCartId());
//        throw new IllegalArgumentException("Product is already in the cart");
//    }

    if (request.getQuantity() <= 0) {
        log.error("Invalid quantity: {}", request.getQuantity());
        throw new IllegalArgumentException("Quantity must be greater than zero");
    }

    if (request.getQuantity() > product.getStockQuantity()){
        log.error("Insufficient stock for product with ID {}. Requested: {}, Available: {}",
                  request.getProductId(), request.getQuantity(), product.getStockQuantity());
        throw new IllegalArgumentException("Insufficient stock for product");
    }
    
    //CartItem builder
    CartItem cartItem = CartItem.builder()
            .cart(cart)
            .product(product)
            .productName(product.getName())
            .quantity(request.getQuantity())
            .build();

    cartItemRepository.save(cartItem);


    log.info("Added product with ID {} to cart with ID {}", request.getProductId(), request.getCartId());

    return CartItemResponse.builder()
            .id(cartItem.getCartItemId())
            .productId(product.getProductId())
            .productName(product.getName())
            .quantity(cartItem.getQuantity())
            .price(product.getPrice().doubleValue())
            .build();
}

    public List<CartItem> getCartItems(Long cartId) {
        cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        return cartItemRepository.findByCartId(cartId);
    }

    public CartItem removeItemFromCart(Long cartId, Long itemId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));

        System.out.println("cartItem.getQuantity() = " + cartItem.getQuantity());

        Product product = cartItem.getProduct();
        // Restore product stock
        product.setStockQuantity(product.getStockQuantity() + cartItem.getQuantity());

        if (!cartItem.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        cartItemRepository.delete(cartItem);

        // Update product stock in the repository
        productRepository.save(product);
        return cartItem;
    }


    @Transactional
    public CartItem updateItemQuantity(Long cartId, Long itemId, int quantity) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        CartItem cartItem = cartItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = cartItem.getProduct();
        if (quantity > product.getStockQuantity()){
            log.error("Insufficient stock for product with ID {}. Requested: {}, Available: {}",
                      product.getProductId(), quantity, product.getStockQuantity());
            throw new IllegalArgumentException("Insufficient stock for product");
        }

        if (quantity == cartItem.getQuantity()) {
            log.info("No change in quantity for item ID: {}", itemId);
            return cartItem; // No change in quantity, return the existing item
        }

        int currentStock ;
        if (quantity > cartItem.getQuantity()) {
            log.info("Increasing quantity for item ID: {} from {} to {}", itemId, cartItem.getQuantity(), quantity);
            currentStock = product.getStockQuantity() - (quantity - cartItem.getQuantity());
        } else {
            log.info("Decreasing quantity for item ID: {} from {} to {}", itemId, cartItem.getQuantity(), quantity);
            currentStock = product.getStockQuantity() + (cartItem.getQuantity() - quantity);
        }


        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        // Update product stock
        product.setStockQuantity(currentStock);
        productRepository.save(product);
        return cartItem;
    }

    public CartItem getCartItemById(Long cartId, Long itemId) {
        cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        return cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));
    }

}
