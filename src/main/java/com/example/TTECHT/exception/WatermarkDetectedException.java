package com.example.TTECHT.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WatermarkDetectedException extends RuntimeException {
    private final List<String> detectedWatermarkIds;
    private final List<Integer> skippedImageIndexes;
    private final List<String> detectedImages;
    private final List<JsonNode> watermarkDetectResponses;
    private final List<String> detectStatuses;
    private final List<String> watermarkBase64s;
    private final List<String> extractedWatermarkBase64s;
    private final String storeName;
    
    public WatermarkDetectedException(String message, String storeName) {
        super(message);
        this.detectedWatermarkIds = new ArrayList<>();
        this.skippedImageIndexes = new ArrayList<>();
        this.detectedImages = new ArrayList<>();
        this.watermarkDetectResponses = new ArrayList<>();
        this.detectStatuses = new ArrayList<>();
        this.watermarkBase64s = new ArrayList<>();
        this.extractedWatermarkBase64s = new ArrayList<>();
        this.storeName = storeName;
    }
    
    public void addDetectedWatermark(String watermarkId, int imageIndex) {
        this.detectedWatermarkIds.add(watermarkId);
        this.skippedImageIndexes.add(imageIndex);
        this.detectedImages.add(null); // Will be set later
        this.watermarkDetectResponses.add(null);
        this.detectStatuses.add(null);
        this.watermarkBase64s.add(null);
        this.extractedWatermarkBase64s.add(null);
    }
    
    public void addDetectedWatermark(String watermarkId, int imageIndex, String imageBase64) {
        this.detectedWatermarkIds.add(watermarkId);
        this.skippedImageIndexes.add(imageIndex);
        this.detectedImages.add(imageBase64);
        this.watermarkDetectResponses.add(null);
        this.detectStatuses.add(null);
        this.watermarkBase64s.add(null);
        this.extractedWatermarkBase64s.add(null);
    }
    
    public void addDetectedWatermark(String watermarkId, int imageIndex, String imageBase64, 
                                   JsonNode watermarkDetectResponse, String detectStatus, String watermarkBase64,
                                   String extractedWatermarkBase64) {
        this.detectedWatermarkIds.add(watermarkId);
        this.skippedImageIndexes.add(imageIndex);
        this.detectedImages.add(imageBase64);
        this.watermarkDetectResponses.add(watermarkDetectResponse);
        this.detectStatuses.add(detectStatus);
        this.watermarkBase64s.add(watermarkBase64);
        this.extractedWatermarkBase64s.add(extractedWatermarkBase64);
    }
    
    public boolean hasDetections() {
        return !detectedWatermarkIds.isEmpty();
    }
    
    public String getDetectionSummary() {
        if (!hasDetections()) {
            return "No watermarks detected";
        }
        
        return String.format("Detected %d watermark(s) in images %s. Watermark IDs: %s", 
            detectedWatermarkIds.size(),
            skippedImageIndexes.toString(),
            detectedWatermarkIds.toString());
    }
}
