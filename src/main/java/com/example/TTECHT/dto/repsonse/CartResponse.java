package com.example.TTECHT.dto.repsonse;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
