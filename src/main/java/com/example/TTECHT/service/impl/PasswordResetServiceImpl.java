package com.example.TTECHT.service.impl;

import com.example.TTECHT.entity.token.PasswordResetToken;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.exception.InvalidTokenException;
import com.example.TTECHT.exception.TooManyRequestsException;
import com.example.TTECHT.repository.token.PasswordResetTokenRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.EmailService;
import com.example.TTECHT.service.PasswordResetService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    UserRepository userRepository;
    PasswordResetTokenRepository passwordResetTokenRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    RedisTemplate<String, String> redisTemplate;


    @Value("${app.password-reset.token-validity-hours:24}")
    int tokenValidityHours;

    @Value("${app.password-reset.max-attempts-per-hour:3}")
    int maxAttemptsPerHour;

    @Override
    public void initiateForgotPassword(String email) {
        log.info("Password reset initiated for email: {}", email);

        if (!isWithinRateLimit(email)) {
            throw new TooManyRequestsException("Too many password reset attempts. Please try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);


        if (userOpt.isPresent()) {
            User user = userOpt.get();

            passwordResetTokenRepository.markAllTokensAsUsedForUser(user);

            String token = generateSecureToken();

            LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenValidityHours);
            PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFirstName());
        }

        incrementRateLimitCounter(email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        // Validate token
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        // can also set the password last changed date if needed
//        user.setPasswordLastChanged(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate all other tokens for this user
        passwordResetTokenRepository.markAllTokensAsUsedForUser(user);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstName());
    }

    public boolean validateToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isExpired() && !resetToken.isUsed())
                .orElse(false);
    }

    private String generateSecureToken() {

        SecureRandom keyGen  = new SecureRandom();
        byte[] randomBytes = new byte[32];
        keyGen.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                randomBytes
        );
    }

    private boolean isWithinRateLimit(String email) {
        String key = "password_reset_attempts:" + email;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts == null || Integer.parseInt(attempts) < maxAttemptsPerHour;
    }

    private void incrementRateLimitCounter(String email) {
        String key = "password_reset_attempts:" + email;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(1));
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
