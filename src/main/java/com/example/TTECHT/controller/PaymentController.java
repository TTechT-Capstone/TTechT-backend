package com.example.TTECHT.controller;

import com.example.TTECHT.dto.payment.CreatePaymentRequest;
import com.example.TTECHT.dto.payment.PaymentDTO;
import com.example.TTECHT.dto.payment.PaymentResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.enumuration.PaymentStatus;
import com.example.TTECHT.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Create Stripe Embedded Checkout Session with Client Secret
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createCheckoutSession(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        try {
            PaymentResponse response = paymentService.createCheckoutSession(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .code(200)
                    .message("Embedded checkout session created successfully")
                    .result(response)
                    .build());
        } catch (StripeException e) {
            log.error("Stripe error: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                    .code(400)
                    .message("Payment processing failed: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error creating checkout session: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Create Payment Intent
     */
    @PostMapping("/intent")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentIntent(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        try {
            PaymentResponse response = paymentService.createPaymentIntent(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .code(200)
                    .message("Payment intent created successfully")
                    .result(response)
                    .build());
        } catch (StripeException e) {
            log.error("Stripe error: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                    .code(400)
                    .message("Payment processing failed: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error creating payment intent: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get payment status by session ID for frontend polling
     */
    @GetMapping("/status/{sessionId}")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(@PathVariable String sessionId) {
        try {
            PaymentDTO payment = paymentService.getPaymentByStripeSessionId(sessionId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("paymentId", payment.getPaymentId());
            status.put("sessionId", sessionId);
            status.put("status", payment.getStatus());
            status.put("amount", payment.getAmount());
            status.put("currency", payment.getCurrency());
            status.put("customerEmail", payment.getCustomerEmail());
            status.put("customerName", payment.getCustomerName());
            status.put("paidAt", payment.getPaidAt());
            status.put("createdAt", payment.getCreatedAt());
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Payment status retrieved successfully")
                .result(status)
                .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                .code(400)
                .message("Payment not found: " + e.getMessage())
                .build());
        }
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable Long paymentId) {
        try {
            PaymentDTO payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(ApiResponse.<PaymentDTO>builder()
                    .code(200)
                    .message("Payment retrieved successfully")
                    .result(payment)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentDTO>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get payment by Stripe Payment Intent ID
     */
    @GetMapping("/intent/{paymentIntentId}")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByIntentId(@PathVariable String paymentIntentId) {
        try {
            PaymentDTO payment = paymentService.getPaymentByStripePaymentIntentId(paymentIntentId);
            return ResponseEntity.ok(ApiResponse.<PaymentDTO>builder()
                    .code(200)
                    .message("Payment retrieved successfully")
                    .result(payment)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentDTO>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get payment by Stripe Session ID
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentBySessionId(@PathVariable String sessionId) {
        try {
            PaymentDTO payment = paymentService.getPaymentByStripeSessionId(sessionId);
            return ResponseEntity.ok(ApiResponse.<PaymentDTO>builder()
                    .code(200)
                    .message("Payment retrieved successfully")
                    .result(payment)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentDTO>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get user's payments
     */
    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getMyPayments(Authentication authentication) {
        try {
            List<PaymentDTO> payments = paymentService.getPaymentsByUser(authentication.getName());
            return ResponseEntity.ok(ApiResponse.<List<PaymentDTO>>builder()
                    .code(200)
                    .message("Payments retrieved successfully")
                    .result(payments)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<PaymentDTO>>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get user's payments with pagination
     */
    @GetMapping("/my-payments/paginated")
    @PreAuthorize("hasRole('USER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getMyPaymentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<PaymentDTO> payments = paymentService.getPaymentsByUser(authentication.getName(), pageable);
            
            return ResponseEntity.ok(ApiResponse.<Page<PaymentDTO>>builder()
                    .code(200)
                    .message("Payments retrieved successfully")
                    .result(payments)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Page<PaymentDTO>>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Update payment status (Admin only)
     */
    @PutMapping("/{paymentIntentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePaymentStatus(
            @PathVariable String paymentIntentId,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String failureReason) {
        try {
            PaymentDTO payment = paymentService.updatePaymentStatus(paymentIntentId, status, failureReason);
            return ResponseEntity.ok(ApiResponse.<PaymentDTO>builder()
                    .code(200)
                    .message("Payment status updated successfully")
                    .result(payment)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentDTO>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get payments by status (Admin only)
     */
    @GetMapping("/status-filter/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        try {
            List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(ApiResponse.<List<PaymentDTO>>builder()
                    .code(200)
                    .message("Payments retrieved successfully")
                    .result(payments)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<PaymentDTO>>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Handle Stripe webhooks
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            HttpServletRequest request,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        log.info("=== WEBHOOK RECEIVED ===");
        log.info("Stripe-Signature: {}", sigHeader);

        if (sigHeader == null) {
            return ResponseEntity.badRequest().body("Missing signature");
        }

        String payload;
        try {
            // Read raw payload to preserve exact bytes
            payload = request.getReader().lines()
                    .collect(Collectors.joining("\n"));
            log.info("Payload length: {}", payload.length());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to read payload");
        }

        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("Webhook failed: ", e);
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }
}
