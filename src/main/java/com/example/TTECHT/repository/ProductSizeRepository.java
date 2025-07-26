package com.example.TTECHT.repository;

import com.example.TTECHT.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    
    List<ProductSize> findByProductProductId(Long productId);
    
    List<ProductSize> findByProductProductIdIn(List<Long> productIds);
    
    @Modifying
    @Query("DELETE FROM ProductSize ps WHERE ps.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    @Query("SELECT ps.size FROM ProductSize ps WHERE ps.product.productId = :productId")
    List<String> findSizesByProductId(@Param("productId") Long productId);
}
