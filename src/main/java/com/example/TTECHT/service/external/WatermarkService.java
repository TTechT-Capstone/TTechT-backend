package com.example.TTECHT.service.external;

import com.example.TTECHT.dto.watermark.WatermarkRequestDTO;
import com.example.TTECHT.dto.watermark.WatermarkResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatermarkService {

    private final RestTemplate restTemplate;

    @Value("${watermark.service.url:http://localhost:8081/api/watermark}")
    private String watermarkServiceUrl;

    /**
     * Calls the watermark service to add watermark to an image
     * 
     * @param imageBase64 Base64 encoded image
     * @param storeName Store name to be used in watermark
     * @return WatermarkResponseDTO containing the image URL or error information
     * @throws RuntimeException if the watermark service call fails
     */
    public WatermarkResponseDTO addWatermark(String imageBase64, String storeName) {
        try {
            // Prepare request
            WatermarkRequestDTO request = new WatermarkRequestDTO(imageBase64, storeName);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WatermarkRequestDTO> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling watermark service for store: {}", storeName);
            
            // Make REST call
            ResponseEntity<WatermarkResponseDTO> response = restTemplate.exchange(
                watermarkServiceUrl,
                HttpMethod.POST,
                entity,
                WatermarkResponseDTO.class
            );
            
            WatermarkResponseDTO responseBody = response.getBody();
            
            if (responseBody != null) {
                log.info("Watermark service response - Success: {}, Message: {}, URL: {}", 
                    responseBody.isSuccess(), responseBody.getMessage(), responseBody.getImageUrl());
                
                if (responseBody.isSuccess() && responseBody.getImageUrl() != null) {
                    return responseBody;
                } else {
                    log.warn("Watermark service returned unsuccessful response or no URL: {}", 
                        responseBody.getMessage());
                    throw new RuntimeException("Watermark service failed: " + responseBody.getMessage());
                }
            } else {
                log.warn("Watermark service returned null response");
                throw new RuntimeException("Watermark service returned empty response");
            }
            
        } catch (RestClientException e) {
            log.error("Failed to call watermark service for store: {}", storeName, e);
            throw new RuntimeException("Failed to communicate with watermark service: " + e.getMessage(), e);
        }
    }
}
