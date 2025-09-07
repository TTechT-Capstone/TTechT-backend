package com.example.TTECHT.repository;

import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {
    
    List<ProductColor> findByProductProductId(Long productId);
    
    List<ProductColor> findByProductProductIdIn(List<Long> productIds);
    
    @Modifying
    @Query("DELETE FROM ProductColor pc WHERE pc.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pc.color FROM ProductColor pc WHERE pc.product.productId = :productId")
    List<String> findColorsByProductId(@Param("productId") Long productId);
    
    Optional<ProductColor> findByProductAndColor(Product product, String color);
}
