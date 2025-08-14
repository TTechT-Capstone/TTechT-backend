package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.repsonse.CartItemResponse;
import com.example.TTECHT.dto.request.CartItemRequest;
import com.example.TTECHT.dto.request.CartItemUpdateRequest;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.entity.cart.CartItem;
import com.example.TTECHT.repository.ProductColorRepository;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.ProductSizeRepository;
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
    ProductColorRepository productColorRepository;
    ProductSizeRepository productSizeRepository;


public CartItemResponse addItemToCart(CartItemRequest request) {
    Cart cart = cartRepository.findById(Long.valueOf(request.getCartId()))
            .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + request.getCartId()));

    Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

    // Validate quantity
    if (request.getQuantity() <= 0) {
        log.error("Invalid quantity: {}", request.getQuantity());
        throw new IllegalArgumentException("Quantity must be greater than zero");
    }

    if (request.getQuantity() > product.getStockQuantity()){
        log.error("Insufficient stock for product with ID {}. Requested: {}, Available: {}",
                  request.getProductId(), request.getQuantity(), product.getStockQuantity());
        throw new IllegalArgumentException("Insufficient stock for product");
    }

    // Validate color if provided
    if (request.getColor() != null && !request.getColor().trim().isEmpty()) {
        boolean colorExists = productColorRepository.findByProductAndColor(product, request.getColor()).isPresent();
        if (!colorExists) {
            log.error("Invalid color '{}' for product with ID {}", request.getColor(), request.getProductId());
            throw new IllegalArgumentException("Invalid color for this product");
        }
    }

    // Validate size if provided
    if (request.getSize() != null && !request.getSize().trim().isEmpty()) {
        boolean sizeExists = productSizeRepository.findByProductAndSize(product, request.getSize()).isPresent();
        if (!sizeExists) {
            log.error("Invalid size '{}' for product with ID {}", request.getSize(), request.getProductId());
            throw new IllegalArgumentException("Invalid size for this product");
        }
    }
    
    //CartItem builder
    CartItem cartItem = CartItem.builder()
            .cart(cart)
            .product(product)
            .productName(product.getName())
            .quantity(request.getQuantity())
            .selectedColor(request.getColor())
            .selectedSize(request.getSize())
            .build();

    cartItemRepository.save(cartItem);

    log.info("Added product with ID {} to cart with ID {} with color: {}, size: {}", 
             request.getProductId(), request.getCartId(), request.getColor(), request.getSize());

    return CartItemResponse.builder()
            .id(cartItem.getCartItemId())
            .productId(product.getProductId())
            .productName(product.getName())
            .quantity(cartItem.getQuantity())
            .price(product.getPrice().doubleValue())
            .selectedColor(cartItem.getSelectedColor())
            .selectedSize(cartItem.getSelectedSize())
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

    @Transactional
    public CartItemResponse updateCartItem(Long cartId, Long itemId, CartItemUpdateRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to the specified cart");
        }

        Product product = cartItem.getProduct();
        boolean updated = false;

        // Update quantity if provided
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                log.error("Invalid quantity: {}", request.getQuantity());
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            if (request.getQuantity() > product.getStockQuantity()) {
                log.error("Insufficient stock for product with ID {}. Requested: {}, Available: {}",
                        product.getProductId(), request.getQuantity(), product.getStockQuantity());
                throw new IllegalArgumentException("Insufficient stock for product");
            }

            if (!request.getQuantity().equals(cartItem.getQuantity())) {
                int currentStock;
                if (request.getQuantity() > cartItem.getQuantity()) {
                    log.info("Increasing quantity for item ID: {} from {} to {}", itemId, cartItem.getQuantity(), request.getQuantity());
                    currentStock = product.getStockQuantity() - (request.getQuantity() - cartItem.getQuantity());
                } else {
                    log.info("Decreasing quantity for item ID: {} from {} to {}", itemId, cartItem.getQuantity(), request.getQuantity());
                    currentStock = product.getStockQuantity() + (cartItem.getQuantity() - request.getQuantity());
                }
                
                cartItem.setQuantity(request.getQuantity());
                product.setStockQuantity(currentStock);
                productRepository.save(product);
                updated = true;
            }
        }

        // Update color if provided
        if (request.getColor() != null) {
            if (!request.getColor().trim().isEmpty()) {
                boolean colorExists = productColorRepository.findByProductAndColor(product, request.getColor()).isPresent();
                if (!colorExists) {
                    log.error("Invalid color '{}' for product with ID {}", request.getColor(), product.getProductId());
                    throw new IllegalArgumentException("Invalid color for this product");
                }
            }
            
            if (!request.getColor().equals(cartItem.getSelectedColor())) {
                cartItem.setSelectedColor(request.getColor());
                updated = true;
                log.info("Updated color for cart item ID: {} to {}", itemId, request.getColor());
            }
        }

        // Update size if provided
        if (request.getSize() != null) {
            if (!request.getSize().trim().isEmpty()) {
                boolean sizeExists = productSizeRepository.findByProductAndSize(product, request.getSize()).isPresent();
                if (!sizeExists) {
                    log.error("Invalid size '{}' for product with ID {}", request.getSize(), product.getProductId());
                    throw new IllegalArgumentException("Invalid size for this product");
                }
            }
            
            if (!request.getSize().equals(cartItem.getSelectedSize())) {
                cartItem.setSelectedSize(request.getSize());
                updated = true;
                log.info("Updated size for cart item ID: {} to {}", itemId, request.getSize());
            }
        }



        if (updated) {
            cartItemRepository.save(cartItem);
            log.info("Successfully updated cart item ID: {} in cart ID: {}", itemId, cartId);
        } else {
            log.info("No changes made to cart item ID: {}", itemId);
        }

        return CartItemResponse.builder()
                .id(cartItem.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .quantity(cartItem.getQuantity())
                .price(product.getPrice().doubleValue())
                .selectedColor(cartItem.getSelectedColor())
                .selectedSize(cartItem.getSelectedSize())
                .build();
    }

    public CartItem getCartItemById(Long cartId, Long itemId) {
        cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
        return cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + itemId));
    }

}
