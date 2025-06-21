package com.example.TTECHT.repository.user;

import com.example.TTECHT.entity.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    Optional<Seller> findByStoreName(String storeName);
    boolean existsByStoreName(String storeName);
    @Query("SELECT COUNT(s) > 0 FROM Seller s WHERE s.storeName = :storeName AND s.user.id != :userId")
    boolean existsByStoreNameExcludingUserId(@Param("storeName") String storeName, @Param("userId") Long userId);

    @Query("SELECT COUNT(s) > 0 FROM Seller s WHERE s.storeName = :storeName AND s.id != :sellerId")
    boolean existsByStoreNameExcludingSellerId(@Param("storeName") String storeName, @Param("sellerId") Long sellerId);
}