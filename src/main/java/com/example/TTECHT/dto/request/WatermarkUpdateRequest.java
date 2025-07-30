package com.example.TTECHT.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatermarkUpdateRequest {
    @NotNull(message = "Store name cannot be null")
    private String watermarkUrlImage;
}
