package com.example.TTECHT.controller;

import com.example.TTECHT.service.PasswordResetService;
import com.example.TTECHT.service.impl.PasswordResetServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class RedisTestController {
    private final PasswordResetService passwordResetService;
    private RedisTemplate<String, String> redisTemplate;

    public RedisTestController(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate, PasswordResetService passwordResetService) {
        this.redisTemplate = redisTemplate;
        this.passwordResetService = passwordResetService;
    }


    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> testRedisOperations() {
        Map<String, Object> result = new HashMap<>();
        String testKey = "debug:test:" + System.currentTimeMillis();

        try {
            // Test 1: Basic set/get
            redisTemplate.opsForValue().set(testKey, "1");
            String getValue = redisTemplate.opsForValue().get(testKey);
            result.put("setGet", "Set: 1, Get: " + getValue);

            // Test 2: Increment operation
            try {
                Long incrementResult = redisTemplate.opsForValue().increment(testKey);
                result.put("increment", "Success: " + incrementResult);
            } catch (Exception e) {
                result.put("increment", "Failed: " + e.getMessage());
            }

            // Test 3: Manual increment
            String currentVal = redisTemplate.opsForValue().get(testKey);
            int manual = Integer.parseInt(currentVal) + 1;
            redisTemplate.opsForValue().set(testKey, String.valueOf(manual));
            String newVal = redisTemplate.opsForValue().get(testKey);
            result.put("manualIncrement", "Before: " + currentVal + ", After: " + newVal);

            // Clean up
            redisTemplate.delete(testKey);

            result.put("status", "success");

        } catch (Exception e) {
            log.error("Redis test failed", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/rate-limit/{email}/status")
    public ResponseEntity<Map<String, Object>> checkRateLimitStatus(@PathVariable String email) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (passwordResetService instanceof PasswordResetServiceImpl) {
                PasswordResetServiceImpl impl = (PasswordResetServiceImpl) passwordResetService;
                int currentAttempts = impl.getCurrentAttempts(email);
                result.put("email", email);
                result.put("currentAttempts", currentAttempts);
                result.put("maxAttempts", 3);
                result.put("remainingAttempts", Math.max(0, 3 - currentAttempts));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}