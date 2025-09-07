package com.example.TTECHT.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatermarkDetectionErrorResponse {
    
    private String errorCode;
    private String message;
    private Long productId;
    private String storeName;
    private List<WatermarkDetectionDetail> detectedWatermarks;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatermarkDetectionDetail {
        private Integer imageIndex;
        private Long watermarkId;
        private String detectionMessage;
        private String detectedImageBase64; // Optional: for debugging
    }
}
