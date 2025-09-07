package com.example.TTECHT.dto.watermark;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkEmbedResponseDTO {

    private JsonNode data;
    private String message;
    private boolean success;
}
