package com.example.TTECHT.dto.repsonse;


import com.example.TTECHT.entity.cart.CartItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    Long id;
    Long userId;
    String promotionCode;
    LocalDateTime submittedTime;
    List<CartItemResponse> cartItems;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
