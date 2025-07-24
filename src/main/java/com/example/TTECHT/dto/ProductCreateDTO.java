package com.example.TTECHT.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {

    @NotNull(message = "StoreName is required")
    private String storeName;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;
    
    // Changed from single fields to arrays
    private List<String> colors; // Can be null, e.g., ["Red", "Blue", "Green"]
    
    private List<String> sizes; // Can be null, e.g., ["S", "M", "L"] or ["L", "XL"]
    
    @NotBlank(message = "Brand is required")
    private String brand;
}
