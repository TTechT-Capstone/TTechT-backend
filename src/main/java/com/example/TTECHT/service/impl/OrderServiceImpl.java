package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.entity.cart.CartItem;
import com.example.TTECHT.entity.order.Order;
import com.example.TTECHT.entity.order.OrderItem;
import com.example.TTECHT.entity.user.User;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public OrderResponse createOrder(Long userId, Long cartId, OrderCreationRequest orderCreationRequest) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Optional<Cart> cart = cartRepository.findByUserId(user.getId());
        if (cart.isEmpty()) {
            log.error("Cart not found for user ID: {}", userId);
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
        }

        if (!cart.get().getId().equals(cartId)) {
            log.error("Cart ID mismatch for user ID: {}, expected: {}, found: {}", userId, cartId, cart.get().getId());
            throw new IllegalArgumentException("Cart ID mismatch for user ID: " + userId);
        }

        String orderName = "ORD-TTECHT" + System.currentTimeMillis();
       
        List <CartItem> cartItems = new ArrayList<>();
        for (String id : orderCreationRequest.getCartItemIds()) {
            CartItem cartItem = cartItemRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new IllegalArgumentException("Cart item with ID: " + id + " not found in the cart for user ID: " + userId));
            cartItems.add(cartItem);
        }

        List<Product> productsInOrders = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                log.error("Insufficient stock for product ID: {}, requested: {}, available: {}",
                        item.getProduct().getProductId(), item.getQuantity(), item.getProduct().getStockQuantity());
                throw new IllegalArgumentException("Insufficient stock for product ID: " + item.getProduct().getProductId());
            }

            // Update product stock
            item.getProduct().setStockQuantity(item.getProduct().getStockQuantity() - item.getQuantity());
            productsInOrders.add(item.getProduct());
            log.info("Updated stock for product ID: {}, new stock: {}", item.getProduct().getProductId(), item.getProduct().getStockQuantity());
        }

        // Save cart items to the cart
        productRepository.saveAll(productsInOrders);

        // Create and save the order first
        Order order = Order.builder()
                .orderNumber(orderName)
                .totalAmount(orderCreationRequest.getTotalAmount())
                .orderStatus(orderCreationRequest.getOrderStatus())
                .contactName(orderCreationRequest.getContactName())
                .contactEmail(orderCreationRequest.getContactEmail())
                .contactPhone(orderCreationRequest.getContactPhone())
                .deliveryAddress(orderCreationRequest.getDeliveryAddress())
                .promotionCode(orderCreationRequest.getPromotionCode())
                .paymentMethod(orderCreationRequest.getPaymentMethod())
                .user(user)
                .createdBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        // Now create OrderItems with proper entity references
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .price(item.getProduct().getPrice().doubleValue())
                    .discountPrice(0.0)
                    .stockCode("bbnbb")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);

        // Set the order items to the order
        order.setOrderItems(orderItems);
        log.info("Creating order for user ID: {}, cart ID: {}", userId, cartId);

        return OrderResponse.builder()
                .id(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .contactName(order.getContactName())
                .contactEmail(order.getContactEmail())
                .contactPhone(order.getContactPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .promotionCode(order.getPromotionCode())
                .paymentMethod(order.getPaymentMethod())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .build();
    }

    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        OrderResponse orderResponse = OrderResponse.builder().id(orderId).orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .contactName(order.getContactName())
                .contactEmail(order.getContactEmail())
                .contactPhone(order.getContactPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .promotionCode(order.getPromotionCode())
                .paymentMethod(order.getPaymentMethod())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .build();

        log.info("Retrieving order with ID: {}", orderId);
        return orderResponse;
    }


    public OrderResponse updateOrder(Long orderId, OrderCreationRequest orderCreationRequest) {
        // Implementation for updating an order
        log.info("Updating order with ID: {}", orderId);
        return null;
    }

    public List<OrderResponse> getOrderByUserId(Long userId) {
        // Implementation for retrieving an order by user ID

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Order> orders = orderRepository.findByUser(user);
        if (orders.isEmpty()) {
            log.error("No orders found for user ID: {}", userId);
            throw new IllegalArgumentException("No orders found for user ID: " + userId);
        }

        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order : orders) {
            OrderResponse orderResponse = OrderResponse.builder()
                    .id(order.getOrderId())
                    .orderNumber(order.getOrderNumber())
                    .orderStatus(order.getOrderStatus())
                    .totalAmount(order.getTotalAmount())
                    .contactName(order.getContactName())
                    .contactEmail(order.getContactEmail())
                    .contactPhone(order.getContactPhone())
                    .deliveryAddress(order.getDeliveryAddress())
                    .promotionCode(order.getPromotionCode())
                    .paymentMethod(order.getPaymentMethod())
                    .createdBy(order.getCreatedBy())
                    .updatedBy(order.getUpdatedBy())
                    .build();
            orderResponses.add(orderResponse);
        }

        log.info("Retrieving orders for user ID: {}", userId);  
        return orderResponses;
    }
}

    
