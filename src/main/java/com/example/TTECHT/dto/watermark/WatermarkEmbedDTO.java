package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkEmbedDTO {

    @JsonProperty("original_image")
    private String original_image = null;

    @JsonProperty("watermark_image")
    private String watermark_image = null;

    @JsonProperty("alpha")
    private double alpha = 0.6;
}
