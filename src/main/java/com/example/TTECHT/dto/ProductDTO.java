package com.example.TTECHT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Boolean isBestSeller;
    private LocalDateTime createdAt;
}
