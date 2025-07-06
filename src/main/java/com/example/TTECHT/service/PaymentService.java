package com.example.TTECHT.service;

import com.example.TTECHT.dto.payment.CreatePaymentRequest;
import com.example.TTECHT.dto.payment.PaymentDTO;
import com.example.TTECHT.dto.payment.PaymentResponse;
import com.example.TTECHT.entity.Payment;
import com.example.TTECHT.enumuration.PaymentStatus;
import com.stripe.exception.StripeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    
    PaymentResponse createCheckoutSession(CreatePaymentRequest request, String username) throws StripeException;
    
    PaymentResponse createPaymentIntent(CreatePaymentRequest request, String username) throws StripeException;
    
    PaymentDTO getPaymentById(Long paymentId);
    
    PaymentDTO getPaymentByStripePaymentIntentId(String paymentIntentId);
    
    PaymentDTO getPaymentByStripeSessionId(String sessionId);
    
    List<PaymentDTO> getPaymentsByUser(String username);
    
    Page<PaymentDTO> getPaymentsByUser(String username, Pageable pageable);
    
    PaymentDTO updatePaymentStatus(String paymentIntentId, PaymentStatus status);
    
    PaymentDTO updatePaymentStatus(String paymentIntentId, PaymentStatus status, String failureReason);
    
    void handleStripeWebhook(String payload, String sigHeader);
    
    List<PaymentDTO> getPaymentsByStatus(PaymentStatus status);
    
    Payment findEntityById(Long paymentId);
    
    Payment findEntityByStripePaymentIntentId(String paymentIntentId);
}
