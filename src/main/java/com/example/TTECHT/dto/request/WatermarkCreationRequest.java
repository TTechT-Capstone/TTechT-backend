package com.example.TTECHT.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkCreationRequest {
    @NotNull(message = "Store name cannot be null")
    private String storeName;

    @NotNull(message = "Watermark URL image cannot be null")
    private String watermarkUrlImage;
}