package com.example.TTECHT.repository.token;


import com.example.TTECHT.entity.token.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    //TODO: Add custom query methods if needed or implement some logic here
    Optional<PasswordResetToken> findByToken(String token);
}
