package com.example.TTECHT.repository;

import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategoryCategoryId(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
           "(:storeName IS NULL OR LOWER(p.storeName) LIKE LOWER(CONCAT('%', :storeName, '%')))")
    Page<Product> findByFilters(@Param("name") String name, 
                               @Param("categoryId") Long categoryId, 
                               @Param("storeName") String storeName, 
                               Pageable pageable);
    
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.storeName = :storeName")
    List<Product> findByStoreName(@Param("storeName") String storeName);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    List<Product> findBySeller(User seller);
    
    // Best seller queries based on sold quantity
    @Query("SELECT p FROM Product p ORDER BY p.soldQuantity DESC")
    List<Product> findBestSellerProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId ORDER BY p.soldQuantity DESC")
    List<Product> findBestSellerProductsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.soldQuantity >= :minSoldQuantity ORDER BY p.soldQuantity DESC")
    List<Product> findProductsWithMinimumSales(@Param("minSoldQuantity") Integer minSoldQuantity, Pageable pageable);
}
