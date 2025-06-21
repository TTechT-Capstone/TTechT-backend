package com.example.TTECHT.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerUpdateRequest {

    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    private String storeName;

    @Size(max = 500, message = "Store description cannot exceed 500 characters")
    private String storeDescription;

    @Override
    public String toString() {
        return "SellerUpdateRequest{" +
                "storeName='" + storeName + '\'' +
                ", storeDescription='" + storeDescription + '\'' +
                '}';
    }
}