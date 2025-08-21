package com.example.TTECHT.dto.repsonse;


import com.example.TTECHT.enumuration.CancellationReason;
import com.example.TTECHT.enumuration.OrderStatus;
import com.example.TTECHT.enumuration.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderResponse {
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;

    // Customer contact information
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String deliveryAddress;

    // Order details
    private String promotionCode;
    private PaymentMethod paymentMethod;

    // Seller-specific totals (only for their products)
    private Double sellerTotal; // Total amount for seller's products only
    private Integer totalSellerItems; // Count of seller's items in the order

    // Only order items that belong to this seller
    private List<SellerOrderItemResponse> sellerOrderItems;

    // Cancellation info (if applicable)
    private CancellationReason cancellationReason;
    private LocalDateTime cancelledAt;
    private String cancelledBy;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Customer info (limited for seller view)
    private Long customerId;
    private String customerName; // First + Last name
}