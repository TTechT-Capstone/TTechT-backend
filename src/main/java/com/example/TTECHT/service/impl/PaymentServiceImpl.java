package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.payment.CreatePaymentRequest;
import com.example.TTECHT.dto.payment.PaymentDTO;
import com.example.TTECHT.dto.payment.PaymentItemDTO;
import com.example.TTECHT.dto.payment.PaymentResponse;
import com.example.TTECHT.entity.Payment;
import com.example.TTECHT.entity.PaymentItem;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.enumuration.PaymentStatus;
import com.example.TTECHT.repository.PaymentRepository;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @Override
    public PaymentResponse createCheckoutSession(CreatePaymentRequest request, String username) throws StripeException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Calculate total amount and prepare line items
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PaymentItem> paymentItems = new ArrayList<>();
        
        for (CreatePaymentRequest.PaymentItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            // Check stock availability
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            // Create Stripe line item
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(product.getPrice().multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(product.getName())
                                                    .setDescription(product.getDescription())
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity(itemRequest.getQuantity().longValue())
                    .build();
            
            lineItems.add(lineItem);
            
            // Prepare payment item
            PaymentItem paymentItem = PaymentItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(itemTotal)
                    .productName(product.getName())
                    .productDescription(product.getDescription())
                    .build();
            
            paymentItems.add(paymentItem);
        }
        
        // Create payment record WITHOUT items first
        Payment payment = Payment.builder()
                .user(user)
                .amount(totalAmount)
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : user.getEmail())
                .customerName(request.getCustomerName() != null ? request.getCustomerName() : 
                             user.getFirstName() + " " + user.getLastName())
                .description(request.getDescription())
                .build();
        
        // Save payment first to get the ID
        Payment savedPayment = paymentRepository.save(payment);
        
        // Now set payment reference in items and set the items
        paymentItems.forEach(item -> item.setPayment(savedPayment));
        savedPayment.setItems(paymentItems);
        
        // Create Stripe Checkout Session for Embedded Checkout
        Map<String, String> metadata = new HashMap<>();
        metadata.put("payment_id", savedPayment.getPaymentId().toString());
        metadata.put("user_id", user.getId().toString());
        metadata.put("customer_email", savedPayment.getCustomerEmail());
        metadata.put("customer_name", savedPayment.getCustomerName());
        
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setRedirectOnCompletion(SessionCreateParams.RedirectOnCompletion.NEVER)
                .addAllLineItem(lineItems)
                .setCustomerEmail(savedPayment.getCustomerEmail())
                .putAllMetadata(metadata)
                .build();
        
        Session session = Session.create(params);
        
        // Update payment with session ID
        savedPayment.setStripeSessionId(session.getId());
        savedPayment.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRepository.save(savedPayment);
        
        return PaymentResponse.builder()
                .sessionId(session.getId())
                .paymentIntentId(session.getPaymentIntent())
                .clientSecret(session.getClientSecret()) // This is the session client secret
                .amount(totalAmount)
                .currency("USD")
                .status(session.getStatus())
                .paymentId(savedPayment.getPaymentId())
                .build();
    }
    
    @Override
    public PaymentResponse createPaymentIntent(CreatePaymentRequest request, String username) throws StripeException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PaymentItem> paymentItems = new ArrayList<>();
        
        for (CreatePaymentRequest.PaymentItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            PaymentItem paymentItem = PaymentItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(itemTotal)
                    .productName(product.getName())
                    .productDescription(product.getDescription())
                    .build();
            
            paymentItems.add(paymentItem);
        }
        
        // Create payment record WITHOUT items first
        Payment payment = Payment.builder()
                .user(user)
                .amount(totalAmount)
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : user.getEmail())
                .customerName(request.getCustomerName() != null ? request.getCustomerName() : 
                             user.getFirstName() + " " + user.getLastName())
                .description(request.getDescription())
                .build();
        
        // Save payment first to get the ID
        Payment savedPayment = paymentRepository.save(payment);
        
        // Now set payment reference in items and set the items
        paymentItems.forEach(item -> item.setPayment(savedPayment));
        savedPayment.setItems(paymentItems);
        
        // Create Stripe PaymentIntent
        Map<String, String> metadata = new HashMap<>();
        metadata.put("payment_id", savedPayment.getPaymentId().toString());
        metadata.put("user_id", user.getId().toString());
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalAmount.multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putAllMetadata(metadata)
                .build();
        
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        
        // Update payment with PaymentIntent ID
        savedPayment.setStripePaymentIntentId(paymentIntent.getId());
        paymentRepository.save(savedPayment);
        
        return PaymentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .status(paymentIntent.getStatus())
                .paymentId(savedPayment.getPaymentId())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long paymentId) {
        Payment payment = findEntityById(paymentId);
        return convertToDTO(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByStripePaymentIntentId(String paymentIntentId) {
        Payment payment = findEntityByStripePaymentIntentId(paymentIntentId);
        return convertToDTO(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByStripeSessionId(String sessionId) {
        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with session ID: " + sessionId));
        return convertToDTO(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return paymentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return paymentRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public PaymentDTO updatePaymentStatus(String paymentIntentId, PaymentStatus status) {
        return updatePaymentStatus(paymentIntentId, status, null);
    }
    
    @Override
    public PaymentDTO updatePaymentStatus(String paymentIntentId, PaymentStatus status, String failureReason) {
        Payment payment = findEntityByStripePaymentIntentId(paymentIntentId);
        
        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(status);
        payment.setFailureReason(failureReason);
        
        if (status == PaymentStatus.SUCCEEDED && oldStatus != PaymentStatus.SUCCEEDED) {
            payment.setPaidAt(LocalDateTime.now());
            
            // Update product stock and sold quantity
            updateProductInventory(payment);
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }
    
    private void updateProductInventory(Payment payment) {
        for (PaymentItem item : payment.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            product.setSoldQuantity(product.getSoldQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }
    
//    @Override
//    public void handleStripeWebhook(String payload, String sigHeader) {
//        try {
//            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
//            log.info("Received webhook event: {}", event.getType());
//
//            switch (event.getType()) {
//                case "checkout.session.completed":
//                    handleCheckoutSessionCompleted(event);
//                    break;
//                case "checkout.session.async_payment_succeeded":
//                    handlePaymentSucceeded(event);
//                    break;
//                case "checkout.session.async_payment_failed":
//                    handlePaymentFailed(event);
//                    break;
//                case "payment_intent.succeeded":
//                    handlePaymentIntentSucceeded(event);
//                    break;
//                case "payment_intent.payment_failed":
//                    handlePaymentIntentFailed(event);
//                    break;
//                default:
//                    log.info("Unhandled event type: {}", event.getType());
//            }
//        } catch (Exception e) {
//            log.error("Error processing webhook: ", e);
//            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
//        }
//    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        log.info("🔔 Processing webhook...");
        log.info("🔑 Webhook secret configured: {}", webhookSecret != null ? "YES" : "NO");
        if (webhookSecret != null && webhookSecret.length() > 8) {
            log.info("🔑 Secret preview: {}****", webhookSecret.substring(0, 8));
        }
        log.info("📝 Signature header: {}", sigHeader);
        log.info("📦 Payload length: {}", payload != null ? payload.length() : 0);

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("✅ Webhook signature verified successfully - Event type: {}", event.getType());
            log.info("🆔 Event ID: {}", event.getId());

            switch (event.getType()) {
                case "checkout.session.completed":
                    log.info("🎯 Processing checkout.session.completed");
                    handleCheckoutSessionCompleted(event);
                    break;
                case "checkout.session.async_payment_succeeded":
                    log.info("🎯 Processing checkout.session.async_payment_succeeded");
                    handlePaymentSucceeded(event);
                    break;
                case "checkout.session.async_payment_failed":
                    log.info("🎯 Processing checkout.session.async_payment_failed");
                    handlePaymentFailed(event);
                    break;
                case "payment_intent.succeeded":
                    log.info("🎯 Processing payment_intent.succeeded");
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    log.info("🎯 Processing payment_intent.payment_failed");
                    handlePaymentIntentFailed(event);
                    break;
                default:
                    log.info("ℹ️ Unhandled event type: {}", event.getType());
            }

            log.info("✅ Webhook processing completed successfully");

        } catch (Exception e) {
            log.error("❌ Webhook processing failed: ", e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }
    }

    /**
     * Handle checkout session completed event
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            log.info("🔄 Starting handleCheckoutSessionCompleted");

            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if (session == null) {
                log.error("❌ Session object is null in webhook event");
                return;
            }

            String sessionId = session.getId();
            String paymentStatus = session.getPaymentStatus();
            String paymentIntentId = session.getPaymentIntent();

            log.info("📋 Session Details:");
            log.info("   Session ID: {}", sessionId);
            log.info("   Payment Status: {}", paymentStatus);
            log.info("   Payment Intent ID: {}", paymentIntentId);
            log.info("   Customer Email: {}", session.getCustomerEmail());

            // Find payment in database
            log.info("🔍 Looking for payment with session ID: {}", sessionId);
            Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));

            log.info("✅ Found payment - ID: {}, Current Status: {}",
                    payment.getPaymentId(), payment.getStatus());

            PaymentStatus oldStatus = payment.getStatus();

            if ("paid".equals(paymentStatus)) {
                log.info("💳 Payment is marked as 'paid', updating to SUCCEEDED");
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaidAt(LocalDateTime.now());

                // Update product inventory only if status changed
                if (oldStatus != PaymentStatus.SUCCEEDED) {
                    log.info("📦 Updating inventory for {} items",
                            payment.getItems() != null ? payment.getItems().size() : 0);
                    updateProductInventory(payment);
                    log.info("✅ Inventory updated successfully");
                } else {
                    log.info("ℹ️ Payment already succeeded, skipping inventory update");
                }

            } else if ("unpaid".equals(paymentStatus)) {
                log.info("⏳ Payment is marked as 'unpaid', keeping as PENDING");
                payment.setStatus(PaymentStatus.PENDING);
            } else {
                log.warn("⚠️ Unexpected payment status: {}", paymentStatus);
            }

            // Save to database
            log.info("💾 Saving payment to database...");
            Payment savedPayment = paymentRepository.save(payment);
            log.info("✅ Payment saved successfully - New Status: {}", savedPayment.getStatus());
            log.info("📅 Updated at: {}", savedPayment.getUpdatedAt());

            log.info("🎉 Successfully processed checkout.session.completed for session: {}", sessionId);

        } catch (Exception e) {
            log.error("❌ Error in handleCheckoutSessionCompleted: ", e);
            throw e;
        }
    }

    /**
     * Handle async payment succeeded
     */
    private void handlePaymentSucceeded(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String sessionId = session.getId();
                log.info("🎯 Processing async payment succeeded for session: {}", sessionId);

                Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                        .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));

                if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    payment.setPaidAt(LocalDateTime.now());
                    updateProductInventory(payment);
                    paymentRepository.save(payment);
                    log.info("✅ Async payment succeeded and inventory updated for session: {}", sessionId);
                } else {
                    log.info("ℹ️ Payment already succeeded for session: {}", sessionId);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error handling async payment succeeded: ", e);
            throw e;
        }
    }

    /**
     * Handle async payment failed
     */
    private void handlePaymentFailed(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String sessionId = session.getId();
                log.info("🎯 Processing async payment failed for session: {}", sessionId);

                Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                        .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));

                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment failed via webhook");
                paymentRepository.save(payment);

                log.info("✅ Payment failed for session: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("❌ Error handling async payment failed: ", e);
            throw e;
        }
    }

    /**
     * Handle payment intent succeeded
     */
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                String paymentIntentId = paymentIntent.getId();
                log.info("🎯 Processing payment intent succeeded: {}", paymentIntentId);

                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found for payment intent: " + paymentIntentId));

                if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    payment.setPaidAt(LocalDateTime.now());
                    updateProductInventory(payment);
                    paymentRepository.save(payment);
                    log.info("✅ Payment intent succeeded and inventory updated: {}", paymentIntentId);
                } else {
                    log.info("ℹ️ Payment already succeeded for payment intent: {}", paymentIntentId);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error handling payment intent succeeded: ", e);
            throw e;
        }
    }

    /**
     * Handle payment intent failed
     */
    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                String paymentIntentId = paymentIntent.getId();
                log.info("🎯 Processing payment intent failed: {}", paymentIntentId);

                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found for payment intent: " + paymentIntentId));

                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(paymentIntent.getLastPaymentError() != null ?
                        paymentIntent.getLastPaymentError().getMessage() : "Payment failed");
                paymentRepository.save(payment);

                log.info("✅ Payment intent failed: {}", paymentIntentId);
            }
        } catch (Exception e) {
            log.error("❌ Error handling payment intent failed: ", e);
            throw e;
        }
    }

//    /**
//     * Update product inventory when payment succeeds
//     */
//    private void updateProductInventory(Payment payment) {
//        try {
//            log.info("📦 Starting inventory update for payment ID: {}", payment.getPaymentId());
//
//            if (payment.getItems() == null || payment.getItems().isEmpty()) {
//                log.warn("⚠️ No items found in payment for inventory update");
//                return;
//            }
//
//            for (PaymentItem item : payment.getItems()) {
//                Product product = item.getProduct();
//                int oldStock = product.getStockQuantity();
//                int oldSold = product.getSoldQuantity();
//
//                // Update stock and sold quantities
//                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
//                product.setSoldQuantity(product.getSoldQuantity() + item.getQuantity());
//
//                log.info("📊 Product {} inventory update:", product.getProductId());
//                log.info("   Stock: {} → {}", oldStock, product.getStockQuantity());
//                log.info("   Sold: {} → {}", oldSold, product.getSoldQuantity());
//
//                Product savedProduct = productRepository.save(product);
//                log.info("✅ Product {} saved successfully", savedProduct.getProductId());
//            }
//
//            log.info("✅ All inventory updates completed");
//        } catch (Exception e) {
//            log.error("❌ Error updating inventory: ", e);
//            throw e;
//        }
//    }
    
//    private void handleCheckoutSessionCompleted(Event event) {
//        try {
//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
//            if (session != null) {
//                String sessionId = session.getId();
//                String paymentStatus = session.getPaymentStatus();
//
//                log.info("Processing checkout session completed: {} with payment status: {}", sessionId, paymentStatus);
//
//                // Update payment status in database
//                Payment payment = paymentRepository.findByStripeSessionId(sessionId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));
//
//                if ("paid".equals(paymentStatus)) {
//                    payment.setStatus(PaymentStatus.SUCCEEDED);
//                    payment.setPaidAt(LocalDateTime.now());
//                    updateProductInventory(payment);
//                    log.info("Payment succeeded and inventory updated for session: {}", sessionId);
//                } else if ("unpaid".equals(paymentStatus)) {
//                    payment.setStatus(PaymentStatus.PENDING);
//                    log.info("Payment pending for session: {}", sessionId);
//                }
//
//                paymentRepository.save(payment);
//            }
//        } catch (Exception e) {
//            log.error("Error handling checkout session completed: ", e);
//        }
//    }
//
//    private void handlePaymentSucceeded(Event event) {
//        try {
//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
//            if (session != null) {
//                String sessionId = session.getId();
//                log.info("Processing async payment succeeded for session: {}", sessionId);
//
//                Payment payment = paymentRepository.findByStripeSessionId(sessionId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));
//
//                payment.setStatus(PaymentStatus.SUCCEEDED);
//                payment.setPaidAt(LocalDateTime.now());
//                updateProductInventory(payment);
//                paymentRepository.save(payment);
//
//                log.info("Async payment succeeded and inventory updated for session: {}", sessionId);
//            }
//        } catch (Exception e) {
//            log.error("Error handling async payment succeeded: ", e);
//        }
//    }
//
//    private void handlePaymentFailed(Event event) {
//        try {
//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
//            if (session != null) {
//                String sessionId = session.getId();
//                log.info("Processing async payment failed for session: {}", sessionId);
//
//                Payment payment = paymentRepository.findByStripeSessionId(sessionId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));
//
//                payment.setStatus(PaymentStatus.FAILED);
//                payment.setFailureReason("Payment failed via webhook");
//                paymentRepository.save(payment);
//
//                log.info("Payment failed for session: {}", sessionId);
//            }
//        } catch (Exception e) {
//            log.error("Error handling async payment failed: ", e);
//        }
//    }
//
//    private void handlePaymentIntentSucceeded(Event event) {
//        try {
//            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
//            if (paymentIntent != null) {
//                String paymentIntentId = paymentIntent.getId();
//                log.info("Processing payment intent succeeded: {}", paymentIntentId);
//
//                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found for payment intent: " + paymentIntentId));
//
//                payment.setStatus(PaymentStatus.SUCCEEDED);
//                payment.setPaidAt(LocalDateTime.now());
//                updateProductInventory(payment);
//                paymentRepository.save(payment);
//
//                log.info("Payment intent succeeded and inventory updated: {}", paymentIntentId);
//            }
//        } catch (Exception e) {
//            log.error("Error handling payment intent succeeded: ", e);
//        }
//    }
//
//    private void handlePaymentIntentFailed(Event event) {
//        try {
//            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
//            if (paymentIntent != null) {
//                String paymentIntentId = paymentIntent.getId();
//                log.info("Processing payment intent failed: {}", paymentIntentId);
//
//                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found for payment intent: " + paymentIntentId));
//
//                payment.setStatus(PaymentStatus.FAILED);
//                payment.setFailureReason(paymentIntent.getLastPaymentError() != null ?
//                    paymentIntent.getLastPaymentError().getMessage() : "Payment failed");
//                paymentRepository.save(payment);
//
//                log.info("Payment intent failed: {}", paymentIntentId);
//            }
//        } catch (Exception e) {
//            log.error("Error handling payment intent failed: ", e);
//        }
//    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payment findEntityById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payment findEntityByStripePaymentIntentId(String paymentIntentId) {
        return paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with PaymentIntent ID: " + paymentIntentId));
    }
    
    private PaymentDTO convertToDTO(Payment payment) {
        List<PaymentItemDTO> itemDTOs = payment.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        
        return PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeSessionId(payment.getStripeSessionId())
                .userId(payment.getUser().getId())
                .username(payment.getUser().getUsername())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .customerEmail(payment.getCustomerEmail())
                .customerName(payment.getCustomerName())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .items(itemDTOs)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }
    
    private PaymentItemDTO convertItemToDTO(PaymentItem item) {
        return PaymentItemDTO.builder()
                .paymentItemId(item.getPaymentItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProductName())
                .productDescription(item.getProductDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
