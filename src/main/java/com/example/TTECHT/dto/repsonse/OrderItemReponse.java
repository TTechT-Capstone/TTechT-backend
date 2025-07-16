package com.example.TTECHT.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemReponse {
    private Long id;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double discountPrice;
    private String stockCode;
    private String createdBy;
    private String updatedBy;
}