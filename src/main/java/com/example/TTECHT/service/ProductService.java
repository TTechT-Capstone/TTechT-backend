package com.example.TTECHT.service;

import com.example.TTECHT.dto.ProductCreateDTO;
import com.example.TTECHT.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
//    List<ProductDTO> getProductsByCategory(Long categoryId);
//
//    List<String> getAllCategories();
//
//
//    List<Map<String, Object>> getLowStockProducts(int threshold);
}
