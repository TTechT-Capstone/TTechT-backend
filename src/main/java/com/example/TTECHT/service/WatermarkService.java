
package com.example.TTECHT.service;

import com.example.TTECHT.dto.request.WatermarkCreationRequest;
import com.example.TTECHT.dto.request.WatermarkUpdateRequest;
import com.example.TTECHT.dto.repsonse.WatermarkResponse;


public interface WatermarkService {

    WatermarkResponse createWatermark(WatermarkCreationRequest request);

    WatermarkResponse getWatermarkByStoreName(String storeName);

    WatermarkResponse updateWatermark(String storeName, WatermarkUpdateRequest request);

    void deleteWatermark(String storeName);
}