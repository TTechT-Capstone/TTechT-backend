package com.example.TTECHT.repository.token;

import com.example.TTECHT.entity.token.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
