package com.example.TTECHT.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderItemResponse {
    private Long orderItemId;

    // Product information
    private Long productId;
    private String productName;
    private String productDescription;
    private String brand;
    private String storeName;

    // Order item details
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice; // quantity * unitPrice
    private Double discountPrice;

    // Product variants
    private String selectedColor;
    private String selectedSize;

    // Stock information
    private String stockCode;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}