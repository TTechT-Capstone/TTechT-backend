package com.example.TTECHT.dto.watermark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for watermark upload requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkUploadDTO {
    
    /**
     * Base64 encoded image string
     */
    private String image;
}
