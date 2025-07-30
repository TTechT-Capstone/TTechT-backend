package com.example.TTECHT.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.example.TTECHT.dto.repsonse.RoleResponse;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
    private List<RoleResponse> userRole;
    private String email;
    private String storeName;
    private String storeDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}