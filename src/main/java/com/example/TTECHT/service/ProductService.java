package com.example.TTECHT.service;

import com.example.TTECHT.dto.ProductCreateDTO;
import com.example.TTECHT.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {

    Page<ProductDTO> getAllProducts(Pageable pageable);

    List<Map<String, Object>> getProductsWithFilters(String category, String search);

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductCreateDTO productCreateDTO, String sellerUsername);

    ProductDTO updateProduct(Long id, ProductCreateDTO productCreateDTO);

    void deleteProduct(Long id);

    List<ProductDTO> searchProductsByName(String name);

    Page<ProductDTO> searchProducts(String name, Long categoryId, String storeName, Pageable pageable);

    List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductDTO> getProductsByStore(String storeName);

    ProductDTO updateStock(Long id, Integer newStock);

    List<ProductDTO> getProductsByCategory(Long categoryId);

    List<ProductDTO> getBestSellerProducts(int limit);
    
    List<ProductDTO> getBestSellerProductsByCategory(Long categoryId, int limit);
    
    List<ProductDTO> getTopSellingProducts(int minSoldQuantity, int limit);
    
    // New arrival methods
    List<ProductDTO> getNewArrivalProducts(int limit);
    
    List<ProductDTO> getNewArrivalProductsByCategory(Long categoryId, int limit);
    
    // Get all products without pagination
    List<ProductDTO> getAllProductsWithoutPagination();
    
    // Get products by user/seller ID
    List<ProductDTO> getProductsByUserId(Long userId);

//    List<ProductDTO> getProductsByCategory(Long categoryId);
//
//    List<String> getAllCategories();
//
//
//    List<Map<String, Object>> getLowStockProducts(int threshold);
}
