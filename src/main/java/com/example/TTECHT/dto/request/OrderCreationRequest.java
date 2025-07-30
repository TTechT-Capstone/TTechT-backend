package com.example.TTECHT.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationRequest {


    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;

    @NotBlank(message = "Contact name cannot be blank")
    private String contactName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String contactEmail;

    @NotBlank(message = "Phone number cannot be blank")
    private String contactPhone;

    @NotBlank(message = "Delivery address cannot be blank")
    private String deliveryAddress;

    private String promotionCode; // Optional

    @NotEmpty(message = "Cart item IDs cannot be empty")
    private List<String> cartItemIds;
}
