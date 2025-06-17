package com.example.TTECHT.service.impl;

import com.example.TTECHT.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    // You can use Redis, Database, or In-Memory store
    // This example uses a simple in-memory approach (not recommended for production)
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public void blacklistToken(String tokenOrJti, Date expiration) {
        blacklistedTokens.put(tokenOrJti, expiration);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenOrJti) {
        Date expiration = blacklistedTokens.get(tokenOrJti);
        if (expiration == null) {
            return false;
        }

        // Remove expired tokens
        if (expiration.before(new Date())) {
            blacklistedTokens.remove(tokenOrJti);
            return false;
        }

        return true;
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
