package com.example.TTECHT.repository;

import com.example.TTECHT.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    @Query("SELECT pi.urlImage FROM ProductImage pi WHERE pi.product.productId = :productId")
    List<String> findImageUrlsByProductId(@Param("productId") Long productId);
    
    List<ProductImage> findByProductProductId(Long productId);
    
    List<ProductImage> findByProductProductIdIn(List<Long> productIds);
    
    List<ProductImage> findByProductProductIdNot(Long productId);
    
    void deleteByProductProductId(Long productId);
}
