package com.example.TTECHT.dto.repsonse;


import java.util.List;
import com.example.TTECHT.enumuration.CancellationReason;
import com.example.TTECHT.enumuration.OrderStatus;
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
    private OrderStatus orderStatus;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String deliveryAddress;
    private String promotionCode;
    private PaymentMethod paymentMethod;
    private List<OrderItemReponse> orderItems;
    private String updatedAt;
    private String createdAt;
    private String createdBy;
    private String updatedBy;
    private CancellationReason cancellationReason;
    private String cancelledAt;
    private String cancelledBy;
}
