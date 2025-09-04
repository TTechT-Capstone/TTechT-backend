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

    @JsonProperty("suspect_image")
    private String suspect_image = null;

    @JsonProperty("sideinfo_json_path")
    private JsonNode sideinfo_json_path;
}
