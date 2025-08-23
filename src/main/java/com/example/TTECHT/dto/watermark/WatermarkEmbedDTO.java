package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkEmbedDTO {

    @JsonProperty("product_image_base64")
    private String originalImageBase64 = null;

    @JsonProperty("watermark_image_base64")
    private String watermarkImageBase64 = null;

    @JsonProperty("alpha")
    private double alpha = 0.6;
}
