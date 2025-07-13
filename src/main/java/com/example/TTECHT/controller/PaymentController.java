package com.example.TTECHT.controller;

import com.example.TTECHT.dto.payment.CreatePaymentRequest;
import com.example.TTECHT.dto.payment.PaymentDTO;
import com.example.TTECHT.dto.payment.PaymentResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.enumuration.PaymentStatus;
import com.example.TTECHT.service.PaymentService;
import com.stripe.exception.StripeException;
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

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Create Stripe Checkout Session
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
                    .message("Checkout session created successfully")
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
    @GetMapping("/status/{status}")
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
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook handled successfully");
        } catch (Exception e) {
            log.error("Error handling webhook: ", e);
            return ResponseEntity.badRequest().body("Webhook handling failed");
        }
    }
}
