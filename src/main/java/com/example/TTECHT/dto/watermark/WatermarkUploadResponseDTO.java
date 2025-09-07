package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO for watermark upload responses from Cloudinary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkUploadResponseDTO {
    
    /**
     * Upload response data containing image details
     */
    private UploadData data;
    
    /**
     * Response message
     */
    private String message;
    
    /**
     * Success status
     */
    private boolean success;
    
    /**
     * Inner class for upload data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadData {
        
        /**
         * Creation timestamp
         */
        @JsonProperty("created_at")
        private OffsetDateTime createdAt;
        
        /**
         * File size in bytes
         */
        @JsonProperty("file_size")
        private Long fileSize;
        
        /**
         * Image format (png, jpg, etc.)
         */
        private String format;
        
        /**
         * Image height in pixels
         */
        private Integer height;
        
        /**
         * Public ID for the uploaded image
         */
        @JsonProperty("public_id")
        private String publicId;
        
        /**
         * Full Cloudinary URL
         */
        private String url;
        
        /**
         * Image width in pixels
         */
        private Integer width;
    }
}
