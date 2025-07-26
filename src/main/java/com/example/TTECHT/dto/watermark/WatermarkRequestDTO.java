package com.example.TTECHT.dto.watermark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkRequestDTO {
    private String image;
    private String storeName;
}
