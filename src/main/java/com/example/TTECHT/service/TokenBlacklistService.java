package com.example.TTECHT.service;

import java.util.Date;

public interface TokenBlacklistService {
    void blacklistToken(String tokenOrJti, Date expiration);
    boolean isTokenBlacklisted(String tokenOrJti);
    void cleanupExpiredTokens(); // Optional: for cleanup
}