package com.example.TTECHT.dto.watermark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkResponseDTO {
    private WatermarkData data;
    private String message;
    private boolean success;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WatermarkData {
        private String created_at;
        private int file_size;
        private String format;
        private int height;
        private String public_id;
        private String url;
        private int width;
    }
    
    // Helper method to get the image URL
    public String getImageUrl() {
        return data != null ? data.getUrl() : null;
    }
}
