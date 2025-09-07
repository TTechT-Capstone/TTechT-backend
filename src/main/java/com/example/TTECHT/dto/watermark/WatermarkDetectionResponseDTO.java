package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkDetectionResponseDTO {
    
    private DetectionData data;
    private String message;
    private boolean success;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectionData {
        @JsonProperty("comparison_results")
        private ComparisonResults comparisonResults;
        @JsonProperty("detection_record")
        private Object detectionRecord; // null in your example
        @JsonProperty("detection_result")
        private DetectionResult detectionResult;
        private Metrics metrics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonResults {
        private double mse;
        private double pcc;
        @JsonProperty("pcc_abs")
        private double pccAbs;
        private double psnr;
        private double ssim;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectionResult {
        @JsonProperty("is_match")
        private boolean isMatch;
        @JsonProperty("pcc_threshold")
        private double pccThreshold;
        @JsonProperty("used_absolute_pcc")
        private boolean usedAbsolutePcc;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private double mse;
        private double pcc;
        @JsonProperty("pcc_abs")
        private double pccAbs;
        private double psnr;
        private double ssim;
    }
    
    // Helper method to check if watermark was detected
    public boolean isWatermarkDetected() {
        return data != null && 
               data.getDetectionResult() != null && 
               data.getDetectionResult().isMatch();
    }
    
    // Helper method to get the match result
    public boolean getIsMatch() {
        return data != null && 
               data.getDetectionResult() != null && 
               data.getDetectionResult().isMatch();
    }
}
