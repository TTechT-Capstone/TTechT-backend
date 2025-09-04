package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkDetectionDTO {

    @JsonProperty("original_watermark")
    private String originalWatermark;

    @JsonProperty("extracted_watermark")
    private String extractedWatermark;

    @JsonProperty("pcc_threshold")
    private Double pccThreshold = 0.20; // optional, defaults to 0.70

    @JsonProperty("save_record")
    private Boolean saveRecord = false; // optional, defaults to false

    @JsonProperty("suspect_image")
    private String suspectImage; // optional, for record saving
    
    // Constructor for basic detection
    public WatermarkDetectionDTO(String originalWatermark, String extractedWatermark) {
        this.originalWatermark = originalWatermark;
        this.extractedWatermark = extractedWatermark;
        this.pccThreshold = 0.20;
        this.saveRecord = false;
    }
}
