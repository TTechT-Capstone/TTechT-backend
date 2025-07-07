package com.example.TTECHT.config;

import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class StripeConfig {
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API initialized with key: {}****", stripeApiKey.substring(0, 8));
    }
}
