package com.example.TTECHT.repository.user;

import com.example.TTECHT.entity.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Optional<Seller> findByStoreName(String storeName);
    boolean existsByStoreName(String storeName);
}