package com.example.TTECHT.dto.request;


import com.example.TTECHT.enumuration.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private Double totalAmount;

    @NotBlank(message = "Order status cannot be blank")
    private String orderStatus;

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

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    private List<String> cartItemIds;
}
