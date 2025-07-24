package com.example.TTECHT.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TTECHT.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findById(Long id);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
}
