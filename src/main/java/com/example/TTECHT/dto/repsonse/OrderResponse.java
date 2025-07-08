package com.example.TTECHT.dto.repsonse;


import com.example.TTECHT.enumuration.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Double totalAmount;
    private String orderStatus;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String deliveryAddress;
    private String promotionCode;
    private PaymentMethod paymentMethod;
    private String createdBy;
    private String updatedBy;
}
