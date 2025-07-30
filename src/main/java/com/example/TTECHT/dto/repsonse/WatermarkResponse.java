package com.example.TTECHT.dto.repsonse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatermarkResponse {
    private Long watermarkId;
    private String storeName;
    private String watermarkUrlImage;
}
