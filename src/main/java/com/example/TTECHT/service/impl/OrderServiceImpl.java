package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.repsonse.OrderResponse;
import com.example.TTECHT.dto.request.OrderCreationRequest;
import com.example.TTECHT.entity.cart.Cart;
import com.example.TTECHT.entity.order.Order;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.enumuration.PaymentMethod;
import com.example.TTECHT.repository.cart.CartRepository;
import com.example.TTECHT.repository.order.OrderRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    CartRepository cartRepository;


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
        PaymentMethod paymentMethod;

        if (orderCreationRequest.getPaymentMethod() == PaymentMethod.CARD) {
            paymentMethod = PaymentMethod.CARD;
        } else {
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
        }

        Order order = Order.builder()
                .totalAmount(orderCreationRequest.getTotalAmount())
                .orderStatus(orderCreationRequest.getOrderStatus())
                .contactName(orderCreationRequest.getContactName())
                .orderNumber(orderName)
                .contactEmail(orderCreationRequest.getContactEmail())
                .contactPhone(orderCreationRequest.getContactPhone())
                .deliveryAddress(orderCreationRequest.getDeliveryAddress())
                .promotionCode(orderCreationRequest.getPromotionCode())
                .paymentMethod(orderCreationRequest.getPaymentMethod())
                .user(user)
                .cart(cart.get())
                .createdBy("system")
                .build();

        orderRepository.save(order);
        log.info("Creating order for user ID: {}, cart ID: {}", userId, cartId);
        return null;
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

    public OrderResponse getOrderByUserId(Long userId) {
        // Implementation for retrieving an order by user ID

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Order order = orderRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for user ID: " + userId));

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

        log.info("Retrieving order for user ID: {}", userId);
        return orderResponse;
    }
}
