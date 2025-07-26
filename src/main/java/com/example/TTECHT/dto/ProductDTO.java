package com.example.TTECHT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String storeName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sellerUsername;
    private String sellerName;
    private Integer soldQuantity;
    
    // Changed from single fields to arrays
    private List<String> colors; // Array of available colors
    private List<String> sizes;  // Array of available sizes
    private List<String> images; // Array of image URLs
    
    private String brand;
    private Boolean isBestSeller;
    private Boolean isNewArrival;
    private LocalDateTime createdAt;
}
