package com.example.TTECHT.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String storeName;
    private String storeDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}