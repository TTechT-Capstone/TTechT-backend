package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkExtractDTO {

    @JsonProperty("product_image_base64")
    private String productImageBase64 = null;

    @JsonProperty("json_image")
    private JsonNode jsonImage;
}
