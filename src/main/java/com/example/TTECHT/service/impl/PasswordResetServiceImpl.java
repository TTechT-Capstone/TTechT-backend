package com.example.TTECHT.service.impl;

import com.example.TTECHT.entity.token.PasswordResetToken;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.exception.InvalidTokenException;
import com.example.TTECHT.exception.TooManyRequestsException;
import com.example.TTECHT.repository.token.PasswordResetTokenRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.EmailService;
import com.example.TTECHT.service.PasswordResetService;
import jakarta.annotation.PostConstruct;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;


//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//@Slf4j
//public class PasswordResetServiceImpl implements PasswordResetService {

//    UserRepository userRepository;
//    PasswordResetTokenRepository passwordResetTokenRepository;
//    PasswordEncoder passwordEncoder;
//    EmailService emailService;
//    RedisTemplate<String, String> redisTemplate;
//
//
//    @Value("${app.password-reset.token-validity-hours:24}")
//    int tokenValidityHours;
//
//    @Value("${app.password-reset.max-attempts-per-hour:3}")
//    int maxAttemptsPerHour;
//
//    public PasswordResetServiceImpl(
//            PasswordResetTokenRepository tokenRepository,
//            UserRepository userRepository,
//            EmailService emailService,
//            PasswordEncoder passwordEncoder,
//            @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
//
//        this.passwordResetTokenRepository = tokenRepository;
//        this.userRepository = userRepository;
//        this.emailService = emailService;
//        this.passwordEncoder = passwordEncoder;
//        this.redisTemplate = redisTemplate;
//
//        log.info("Password reset service initialized");
//    }
//
//
//    @PostConstruct
//    public void validateDependencies() {
//        if (redisTemplate == null) {
//            log.error("RedisTemplate is not injected properly");
//            throw new IllegalStateException("RedisTemplate not properly injected");
//        }
//        log.info("RedisTemplate is properly injected");
//    }
//
//    @Override
//    public void initiateForgotPassword(String email) {
//        log.info("Password reset initiated for email: {}", email);
//
//        if (!isWithinRateLimit(email)) {
//            throw new TooManyRequestsException("Too many password reset attempts. Please try again later.");
//        }
//
//        Optional<User> userOpt = userRepository.findByEmail(email);
//
//
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//
//            passwordResetTokenRepository.markAllTokensAsUsedForUser(user);
//
//            String token = generateSecureToken();
//
//            LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenValidityHours);
//            PasswordResetToken resetToken = PasswordResetToken.builder()
//                    .token(token)
//                    .user(user)
//                    .expiryDate(expiryDate)
//                    .build();
//            passwordResetTokenRepository.save(resetToken);
//
//            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFirstName());
//        }
//
//        incrementRateLimitCounter(email);
//    }
//
//    @Override
//    public void resetPassword(String token, String newPassword) {
//        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
//                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));
//
//        // Validate token
//        if (resetToken.isUsed()) {
//            throw new InvalidTokenException("Reset token has already been used");
//        }
//
//        if (resetToken.isExpired()) {
//            throw new InvalidTokenException("Reset token has expired");
//        }
//
//        User user = resetToken.getUser();
//
//        // Update password
//        user.setPassword(passwordEncoder.encode(newPassword));
//        // can also set the password last changed date if needed
////        user.setPasswordLastChanged(LocalDateTime.now());
//        userRepository.save(user);
//
//        // Mark token as used
//        resetToken.setUsed(true);
//        passwordResetTokenRepository.save(resetToken);
//
//        // Invalidate all other tokens for this user
//        passwordResetTokenRepository.markAllTokensAsUsedForUser(user);
//
//        // Send confirmation email
//        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstName());
//    }
//
//    public boolean validateToken(String token) {
//        return passwordResetTokenRepository.findByToken(token)
//                .map(resetToken -> !resetToken.isExpired() && !resetToken.isUsed())
//                .orElse(false);
//    }
//
//    private String generateSecureToken() {
//
//        SecureRandom keyGen  = new SecureRandom();
//        byte[] randomBytes = new byte[32];
//        keyGen.nextBytes(randomBytes);
//        return Base64.getUrlEncoder().withoutPadding().encodeToString(
//                randomBytes
//        );
//    }
//
//
//    private boolean isWithinRateLimit(String email) {
//        try {
//            String key = "password_reset_attempts:" + email;
//            String attempts = redisTemplate.opsForValue().get(key);
//            boolean withinLimit = attempts == null || Integer.parseInt(attempts) < maxAttemptsPerHour;
//            log.info("Password reset attempts for email {}: {}", email, attempts);
//            return withinLimit;
//        } catch (Exception e) {
//            log.error("Error checking rate limit for email {}: {}", email, e.getMessage());
//            // If Redis fails, allow the request to proceed (fail open)
//            return true;
//        }
//    }
//
//    private void incrementRateLimitCounter(String email) {
//        try {
//            String key = "password_reset_attempts:" + email;
//            redisTemplate.opsForValue().increment(key);
//            redisTemplate.expire(key, Duration.ofHours(1));
//            log.debug("Incremented rate limit counter for: {}", email);
//        } catch (Exception e) {
//            log.error("Error incrementing rate limit counter for email: {}", email, e);
//            // Don't throw exception if Redis fails
//        }
//    }
//
//
//    @Scheduled(fixedRate = 3600000) // Run every hour
//    public void cleanupExpiredTokens() {
//        try {
//            passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
//            log.info("Cleaned up {} expired password reset tokens");
//        } catch (Exception e) {
//            log.error("Error during token cleanup", e);
//        }
//    }

//}

@Service
@Transactional
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.password-reset.token-validity-hours:24}")
    private int tokenValidityHours;

    @Value("${app.password-reset.max-attempts-per-hour:3}")
    private int maxAttemptsPerHour;

    public PasswordResetServiceImpl(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;

        log.info("PasswordResetService constructor called");
        log.info("RedisTemplate injected: {}", redisTemplate != null);
    }

    @PostConstruct
    public void validateDependencies() {
        log.info("Validating dependencies...");

        if (redisTemplate == null) {
            log.error("RedisTemplate is null!");
            throw new IllegalStateException("RedisTemplate not properly injected");
        }

        // Try to test Redis connection and operations
        try {
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("Redis ping result: {}", pingResult);

            // Test set/get/increment operations
            String testKey = "test:startup:" + System.currentTimeMillis();

            // Test basic set/get
            redisTemplate.opsForValue().set(testKey, "1");
            String getValue = redisTemplate.opsForValue().get(testKey);
            log.info("Set/Get test: {}", getValue);

            // Test increment - this is what was failing
            try {
                Long incrementResult = redisTemplate.opsForValue().increment(testKey);
                log.info("Increment test result: {}", incrementResult);
            } catch (Exception e) {
                log.warn("Increment test failed, will use manual increment: {}", e.getMessage());
            }

            // Clean up
            redisTemplate.delete(testKey);

            log.info("✅ Redis operations test successful");
        } catch (Exception e) {
            log.error("❌ Redis connection test failed", e);
            throw new IllegalStateException("Redis connection required but not available", e);
        }

        log.info("All dependencies validated successfully");
    }

    @Override
    public void initiateForgotPassword(String email) {
        log.info("Initiating forgot password for email: {}", email);

        // Rate limiting check
        if (!isWithinRateLimit(email)) {
            log.warn("Rate limit exceeded for email: {}", email);
            throw new TooManyRequestsException("Too many password reset attempts. Please try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Invalidate existing tokens
            tokenRepository.markAllTokensAsUsedForUser(user);

            // Generate and save new token
            String token = generateSecureToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenValidityHours);
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(expiryDate)
                    .build();
            tokenRepository.save(resetToken);

            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFirstName());

            log.info("Password reset token created for user: {}", user.getEmail());
        } else {
            log.info("Password reset requested for non-existent email: {}", email);
        }

        // Record attempt (this was causing the NullPointerException)
        recordAttempt(email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Attempting to reset password with token");

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
//        user.setPasswordLastChanged(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Invalidate all other tokens
        tokenRepository.markAllTokensAsUsedForUser(user);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstName());

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isUsed() && !resetToken.isExpired())
                .orElse(false);
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isWithinRateLimit(String email) {
        try {
            String key = "password_reset_attempts:" + email;
            String attemptsStr = redisTemplate.opsForValue().get(key);

            int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;
            boolean withinLimit = attempts < maxAttemptsPerHour;

            log.debug("Rate limit check for {}: {} attempts, within limit: {}", email, attempts, withinLimit);
            return withinLimit;

        } catch (Exception e) {
            log.error("Error checking rate limit for email: {}", email, e);
            // For debugging, let's allow the request if Redis fails
            log.warn("Allowing request due to Redis error");
            return true;
        }
    }

    // FIXED: More robust attempt recording with proper null handling
    private void recordAttempt(String email) {
        try {
            String key = "password_reset_attempts:" + email;
            log.debug("Recording attempt for email: {} with key: {}", email, key);

            // Method 1: Try using increment (preferred)
            try {
                Long newCount = redisTemplate.opsForValue().increment(key);
                if (newCount != null) {
                    // Set expiration on first attempt
                    if (newCount == 1) {
                        redisTemplate.expire(key, Duration.ofHours(1));
                    }
                    log.debug("Recorded attempt #{} for email: {} using increment", newCount, email);
                    return;
                }
            } catch (Exception incrementError) {
                log.warn("Increment failed for key: {}, error: {}", key, incrementError.getMessage());
            }

            // Method 2: Fallback to manual increment
            log.debug("Using manual increment for key: {}", key);
            String currentValue = redisTemplate.opsForValue().get(key);

            if (currentValue == null) {
                // First attempt
                redisTemplate.opsForValue().set(key, "1", Duration.ofHours(1));
                log.debug("Set initial attempt count to 1 for email: {}", email);
            } else {
                // Increment manually
                try {
                    int current = Integer.parseInt(currentValue);
                    int newCount = current + 1;

                    // Get current TTL to preserve it
                    Long ttl = redisTemplate.getExpire(key);
                    Duration expiration = (ttl != null && ttl > 0) ?
                            Duration.ofSeconds(ttl) : Duration.ofHours(1);

                    redisTemplate.opsForValue().set(key, String.valueOf(newCount), expiration);
                    log.debug("Manually incremented attempt count to {} for email: {}", newCount, email);
                } catch (NumberFormatException e) {
                    log.error("Invalid number format in Redis for key: {}, value: {}", key, currentValue);
                    // Reset to 1 if corrupted
                    redisTemplate.opsForValue().set(key, "1", Duration.ofHours(1));
                }
            }

        } catch (Exception e) {
            log.error("Error recording attempt for email: {}", email, e);
            // Don't throw exception to avoid breaking the password reset flow
            // Just log the error and continue
        }
    }

    // Admin helper methods
    public int getCurrentAttempts(String email) {
        try {
            String key = "password_reset_attempts:" + email;
            String attemptsStr = redisTemplate.opsForValue().get(key);
            return (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;
        } catch (Exception e) {
            log.error("Error getting current attempts for email: {}", email, e);
            return 0;
        }
    }

    public void clearAttempts(String email) {
        try {
            String key = "password_reset_attempts:" + email;
            redisTemplate.delete(key);
            log.info("Cleared attempts for email: {}", email);
        } catch (Exception e) {
            log.error("Error clearing attempts for email: {}", email, e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        try {
             tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
//            log.info("Cleaned up {} expired password reset tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }
}
