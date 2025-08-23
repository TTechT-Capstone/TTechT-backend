package com.example.TTECHT.dto.watermark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkDetectionDTO {
    
    private String originalWatermark;
    private String extractedWatermark;
    private Double pccThreshold = 0.70; // optional, defaults to 0.70
    private Boolean saveRecord = false; // optional, defaults to false
    private String suspectImage; // optional, for record saving
    
    // Constructor for basic detection
    public WatermarkDetectionDTO(String originalWatermark, String extractedWatermark) {
        this.originalWatermark = originalWatermark;
        this.extractedWatermark = extractedWatermark;
        this.pccThreshold = 0.70;
        this.saveRecord = false;
    }
}
