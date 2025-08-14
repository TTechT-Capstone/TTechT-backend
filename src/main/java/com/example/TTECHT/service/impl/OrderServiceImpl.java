package com.example.TTECHT.service.impl;

import com.example.TTECHT.constant.OrderConstants;
import com.example.TTECHT.dto.repsonse.OrderItemReponse;
import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.CancelOrderRequest;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.dto.request.UpdateOrderStatusRequest;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.entity.cart.CartItem;
import com.example.TTECHT.entity.order.Order;
import com.example.TTECHT.entity.order.OrderItem;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.enumuration.OrderStatus;
import com.example.TTECHT.enumuration.PaymentMethod;
import com.example.TTECHT.exception.AppException;
import com.example.TTECHT.exception.ErrorCode;
import com.example.TTECHT.mapper.OrderMapper;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.cart.CartItemRepository;
import com.example.TTECHT.repository.cart.CartRepository;
import com.example.TTECHT.repository.order.OrderItemRepository;
import com.example.TTECHT.repository.order.OrderRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    OrderItemRepository orderItemRepository;
    OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(Long userId, Long cartId, OrderCreationRequest orderCreationRequest) {
        try {
            log.info("Creating order for user ID: {}, cart ID: {}", userId, cartId);
            
            // Validate input
            validateOrderCreationRequest(orderCreationRequest);
            
            // Get user and validate
            User user = getUserById(userId);
            
            // Get cart and validate
            Cart cart = getAndValidateCart(userId, cartId);
            
            // Get and validate cart items
            List<CartItem> cartItems = getAndValidateCartItems(orderCreationRequest.getCartItemIds(), userId);
            
            // Validate stock availability and update stock
            validateAndUpdateStock(cartItems);
            
            // Create order
            Order order = createOrder(user, orderCreationRequest);
            
            // Create order items
            List<OrderItem> orderItems = createOrderItems(order, cartItems);
            
            // Clean up cart items after successful order creation
            cleanupCartItems(cartItems);
            
            // Set order items to order for response
            order.setOrderItems(orderItems);
            
            log.info("Successfully created order {} for user ID: {}", order.getOrderNumber(), userId);
            return orderMapper.toOrderResponse(order);
            
        } catch (AppException e) {
            log.error("Failed to create order for user ID: {}, error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating order for user ID: {}", userId, e);
            throw new AppException(ErrorCode.ORDER_CREATION_FAILED);
        }
    }
    
    private void validateOrderCreationRequest(OrderCreationRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_ORDER_DATA);
        }
        
        if (CollectionUtils.isEmpty(request.getCartItemIds())) {
            throw new AppException(ErrorCode.INVALID_ORDER_DATA);
        }
        
        if (request.getTotalAmount() == null || request.getTotalAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_ORDER_DATA);
        }
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    
    private Cart getAndValidateCart(Long userId, Long cartId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        if (!cart.getId().equals(cartId)) {
            log.error("Cart ID mismatch for user ID: {}, expected: {}, found: {}", userId, cartId, cart.getId());
            throw new AppException(ErrorCode.CART_ID_MISMATCH);
        }
        
        return cart;
    }
    
    private List<CartItem> getAndValidateCartItems(List<String> cartItemIds, Long userId) {
        List<CartItem> cartItems = new ArrayList<>();
        
        for (String id : cartItemIds) {
            try {
                Long cartItemId = Long.parseLong(id);
                CartItem cartItem = cartItemRepository.findById(cartItemId)
                        .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
                
                // Validate that cart item belongs to the user
                if (!cartItem.getCart().getUser().getId().equals(userId)) {
                    throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
                }
                
                cartItems.add(cartItem);
            } catch (NumberFormatException e) {
                throw new AppException(ErrorCode.INVALID_CART_ITEM_ID);
            }
        }
        
        return cartItems;
    }
    
    private void validateAndUpdateStock(List<CartItem> cartItems) {
        List<Product> productsToUpdate = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            
            if (product.getStockQuantity() < item.getQuantity()) {
                log.error("Insufficient stock for product ID: {}, requested: {}, available: {}",
                        product.getProductId(), item.getQuantity(), product.getStockQuantity());
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            
            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productsToUpdate.add(product);
            
            log.info("Updated stock for product ID: {}, new stock: {}", 
                    product.getProductId(), product.getStockQuantity());
        }
        
        // Save all updated products in batch
        productRepository.saveAll(productsToUpdate);
    }
    
    private Order createOrder(User user, OrderCreationRequest request) {
        String orderNumber = generateOrderNumber();
        LocalDateTime now = LocalDateTime.now();
        
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .totalAmount(request.getTotalAmount())
                .orderStatus(OrderStatus.NEW)
                .contactName(request.getContactName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .promotionCode(request.getPromotionCode())
                .paymentMethod(PaymentMethod.CARD)
                .user(user)
                .createdBy(OrderConstants.DEFAULT_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        return orderRepository.save(order);
    }
    
    private List<OrderItem> createOrderItems(Order order, List<CartItem> cartItems) {
        LocalDateTime now = LocalDateTime.now();
        
        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .price(item.getProduct().getPrice().doubleValue())
                        .discountPrice(OrderConstants.DEFAULT_DISCOUNT_PRICE)
                        .stockCode(OrderConstants.DEFAULT_STOCK_CODE)
                        .selectedColor(item.getSelectedColor())
                        .selectedSize(item.getSelectedSize())
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .collect(Collectors.toList());
        
        log.info("Created {} order items with cart item attributes (color, size, images)", orderItems.size());
        return orderItemRepository.saveAll(orderItems);
    }
    
    private void cleanupCartItems(List<CartItem> cartItems) {
        try {
            cartItemRepository.deleteAll(cartItems);
            log.info("Cleaned up {} cart items after order creation", cartItems.size());
        } catch (Exception e) {
            log.warn("Failed to cleanup cart items after order creation: {}", e.getMessage());
            // Don't throw exception here as order is already created successfully
        }
    }
    
    private String generateOrderNumber() {
        return OrderConstants.ORDER_NUMBER_PREFIX + System.currentTimeMillis();
    }

    public OrderResponse getOrder(Long orderId) {
        log.info("Retrieving order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(orderId);
        List<OrderItemReponse> orderItemResponses = orderItems.stream()
                .map(item -> OrderItemReponse.builder()
                        .id(item.getOrderItemId())
                        .productName(item.getProduct().getName())
                        .price(item.getProduct().getPrice().doubleValue())
                        .quantity(item.getQuantity())
                        .discountPrice(item.getDiscountPrice())
                        .stockCode(item.getStockCode())
                        .selectedColor(item.getSelectedColor())
                        .selectedSize(item.getSelectedSize())
                        .createdBy(item.getCreatedAt().toString())
                        .updatedBy(item.getUpdatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .contactName(order.getContactName())
                .contactEmail(order.getContactEmail())
                .contactPhone(order.getContactPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .promotionCode(order.getPromotionCode())
                .paymentMethod(order.getPaymentMethod())
                .orderItems(orderItemResponses)
                .createdAt(order.getCreatedAt().toString())
                .updatedBy(order.getUpdatedAt().toString())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .build();
    }


    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderCreationRequest orderCreationRequest) {
        log.info("Updating order with ID: {}", orderId);
        
        // Validate input
        validateOrderCreationRequest(orderCreationRequest);
        
        // Get existing order
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        // Update order details (only non-product related fields)
        // existingOrder.setOrderStatus(orderCreationRequest.getOrderStatus());
        existingOrder.setContactName(orderCreationRequest.getContactName());
        existingOrder.setContactEmail(orderCreationRequest.getContactEmail());
        existingOrder.setContactPhone(orderCreationRequest.getContactPhone());
        existingOrder.setDeliveryAddress(orderCreationRequest.getDeliveryAddress());
        existingOrder.setPromotionCode(orderCreationRequest.getPromotionCode());
        // existingOrder.setPaymentMethod(orderCreationRequest.getPaymentMethod());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(existingOrder);
        
        log.info("Successfully updated order with ID: {}", orderId);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, UpdateOrderStatusRequest orderStatusRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setOrderStatus(OrderStatus.valueOf(orderStatusRequest.getOrderStatus()));
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, CancelOrderRequest request) {
        log.info("Cancelling order with ID: {} with reason: {}", orderId, request.getCancellationReason().getDescription());
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        // Check if order can be cancelled (only if not already cancelled or completed)
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            log.error("Order with ID {} is already cancelled", orderId);
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            log.error("Cannot cancel order with ID {} as it is already completed", orderId);
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }
        
        // Restore product stock when cancelling
        restoreProductStockOnCancellation(order);
        
        // Update order status and cancellation details
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(request.getCancellationReason());
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(request.getCancelledBy() != null ? request.getCancelledBy() : "SYSTEM");
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.save(order);
        
        log.info("Successfully cancelled order with ID: {}", orderId);
    }
    
    private void restoreProductStockOnCancellation(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<Product> productsToUpdate = new ArrayList<>();
        
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            int restoredStock = product.getStockQuantity() + item.getQuantity();
            product.setStockQuantity(restoredStock);
            productsToUpdate.add(product);
            
            log.info("Restored {} units of product ID: {} (new stock: {})", 
                    item.getQuantity(), product.getProductId(), restoredStock);
        }
        
        // Save all updated products in batch
        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
            log.info("Restored stock for {} products after order cancellation", productsToUpdate.size());
        }
    }

    @Transactional
    public List<OrderResponse> getOrderByUserId(Long userId) {
        log.info("Retrieving orders for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Order> orders = orderRepository.findByUser(user);
        if (orders.isEmpty()) {
            log.info("No orders found for user ID: {}", userId);
            return new ArrayList<>(); // Return empty list instead of throwing exception
        }

        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }
}

    
