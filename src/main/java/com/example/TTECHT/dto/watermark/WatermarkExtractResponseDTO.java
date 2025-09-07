package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for watermark extraction responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkExtractResponseDTO {
    
    /**
     * Success status
     */
    private boolean success;
    
    /**
     * Response message
     */
    private String message;
    
    /**
     * Extraction status
     */
    private String status;
    
    /**
     * Extraction data containing watermark details
     */
    private ExtractData data;
    
    /**
     * Inner class for extraction data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractData {
        
        /**
         * Base64 encoded extracted watermark
         */
        @JsonProperty("extracted_watermark")
        private String extractedWatermark;
        
        /**
         * Generated unique identifier
         */
        @JsonProperty("unique_id")
        private String uniqueId;
        
        /**
         * Alpha value used for extraction
         */
        private Double alpha;
        
        /**
         * Wavelet type used (e.g., "haar")
         */
        private String wavelet;
        
        /**
         * Canonical size as [width, height]
         */
        @JsonProperty("canonical_size")
        private List<Integer> canonicalSize;
        
        /**
         * Path to the sideinfo file used
         */
        @JsonProperty("sideinfo_used")
        private String sideinfoUsed;
        
        /**
         * Path to the original watermark logo
         */
        @JsonProperty("watermark_logo")
        private String watermarkLogo;
        
        /**
         * Path to the extracted watermark file
         */
        @JsonProperty("extracted_path")
        private String extractedPath;
    }
}
