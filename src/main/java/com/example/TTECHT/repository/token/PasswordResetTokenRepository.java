package com.example.TTECHT.repository.token;


import com.example.TTECHT.entity.token.PasswordResetToken;
import com.example.TTECHT.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    //TODO: Add custom query methods if needed or implement some logic here
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndUsedFalse(User user);
    void deleteByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = :user")
    void markAllTokensAsUsedForUser(@Param("user") User user);
}
