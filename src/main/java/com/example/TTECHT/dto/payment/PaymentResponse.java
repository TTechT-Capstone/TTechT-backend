package com.example.TTECHT.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String sessionId;
    private String paymentIntentId;
    private String clientSecret;
    private String checkoutUrl;
    private String status;
    private Long paymentId;
    private BigDecimal amount;
    private String currency;
}
