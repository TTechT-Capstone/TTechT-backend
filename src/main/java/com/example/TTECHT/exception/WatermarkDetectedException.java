package com.example.TTECHT.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WatermarkDetectedException extends RuntimeException {
    private final List<String> detectedWatermarkIds;
    private final List<Integer> skippedImageIndexes;
    private final List<String> detectedImages;
    private final String storeName;
    
    public WatermarkDetectedException(String message, String storeName) {
        super(message);
        this.detectedWatermarkIds = new ArrayList<>();
        this.skippedImageIndexes = new ArrayList<>();
        this.detectedImages = new ArrayList<>();
        this.storeName = storeName;
    }
    
    public void addDetectedWatermark(String watermarkId, int imageIndex) {
        this.detectedWatermarkIds.add(watermarkId);
        this.skippedImageIndexes.add(imageIndex);
        this.detectedImages.add(null); // Will be set later
    }
    
    public void addDetectedWatermark(String watermarkId, int imageIndex, String imageBase64) {
        this.detectedWatermarkIds.add(watermarkId);
        this.skippedImageIndexes.add(imageIndex);
        this.detectedImages.add(imageBase64);
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
