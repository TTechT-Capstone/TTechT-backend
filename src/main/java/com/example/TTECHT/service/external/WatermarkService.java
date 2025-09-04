package com.example.TTECHT.service.external;

import com.example.TTECHT.dto.watermark.WatermarkEmbedDTO;
import com.example.TTECHT.dto.watermark.WatermarkEmbedResponseDTO;
import com.example.TTECHT.dto.watermark.WatermarkExtractDTO;
import com.example.TTECHT.dto.watermark.WatermarkRequestDTO;
import com.example.TTECHT.dto.watermark.WatermarkResponseDTO;
import com.example.TTECHT.dto.watermark.WatermarkDetectionDTO;
import com.example.TTECHT.dto.watermark.WatermarkDetectionResponseDTO;
import com.example.TTECHT.dto.watermark.WatermarkUploadDTO;
import com.example.TTECHT.dto.watermark.WatermarkUploadResponseDTO;
import com.example.TTECHT.dto.watermark.WatermarkExtractResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${watermark.service.url:http://localhost:8081/api/images}")
    private String watermarkServiceUrl;

    @Value("${image.service.url:http://localhost:8081/api/images}") 
    private String imageServiceUrl;

    @Value("${upload.service.url:http://localhost:8081/api/images}")
    private String uploadServiceUrl;

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
                watermarkServiceUrl + "",
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

    /**
     * Calls the image service to extract watermark from an image
     * 
     * @param imageBase64 Base64 encoded image
     * @param jsonImage JSON image
     * @return WatermarkExtractResponseDTO containing the extracted watermark data
     * @throws RuntimeException if the image service call fails
     */

    public WatermarkExtractResponseDTO extractWatermark(String imageBase64, JsonNode jsonImage) {
        try {

            // prepare request
            WatermarkExtractDTO request = new WatermarkExtractDTO(imageBase64, jsonImage);

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WatermarkExtractDTO> entity = new HttpEntity<>(request, headers);

            log.info("Calling image service to extract watermark");

            // make REST call
            ResponseEntity<WatermarkExtractResponseDTO> response = restTemplate.exchange(
                imageServiceUrl + "/extract",
                HttpMethod.POST,
                entity, WatermarkExtractResponseDTO.class);

            WatermarkExtractResponseDTO responseBody = response.getBody();
            log.info("Image service response - Success: {}, Status: {}, Extracted watermark length: {}", 
                responseBody != null ? responseBody.isSuccess() : "null", 
                responseBody != null ? responseBody.getStatus() : "null",
                responseBody != null && responseBody.getData() != null ? 
                    (responseBody.getData().getExtractedWatermark() != null ? 
                        responseBody.getData().getExtractedWatermark().length() : 0) : 0);

            return response.getBody();
        }
        catch (Exception e) {
            log.error("Failed to call watermark service for store: {}", imageBase64, e);
            throw new RuntimeException("Failed to communicate with watermark service: " + e.getMessage(), e);
        }   
    }


    /**
     * Calls the image service to embed watermark to an image
     * 
     * @param originalImageBase64 Base64 encoded original image
     * @param watermarkImageBase64 Base64 encoded watermark image
     * @param alpha Alpha value for the watermark
     * @return WatermarkEmbedDTO containing the embedded image
     * @throws RuntimeException if the image service call fails
     */

    public WatermarkEmbedResponseDTO embedWatermark(String originalImageBase64, String watermarkImageBase64, double alpha) {
        try {
            // prepare request
            WatermarkEmbedDTO request = new WatermarkEmbedDTO(originalImageBase64, watermarkImageBase64, alpha);

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WatermarkEmbedDTO> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling image service to embed watermark");

            // make REST call
            ResponseEntity<WatermarkEmbedResponseDTO> response = restTemplate.exchange(
                imageServiceUrl + "/embed",
                HttpMethod.POST,
                entity, WatermarkEmbedResponseDTO.class);

            WatermarkEmbedResponseDTO responseBody = response.getBody();
            log.info("Image service response - Original image base64: {}, Watermark image base64: {}", 
                responseBody != null ? responseBody.getData() : "null");

            return response.getBody();
        }
        catch (Exception e) {
            log.error("Failed to call watermark service for store: {}", originalImageBase64, e);
            throw new RuntimeException("Failed to communicate with watermark service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calls the image service to detect if a watermark exists in an image
     * 
     * @param extractedWatermark Base64 encoded extracted watermark
     * @param originalWatermark Base64 encoded original watermark image to compare against
     * @return boolean indicating if watermark was detected
     * @throws RuntimeException if the image service call fails
     */
    public boolean detectWatermark(String originalWatermark, String extractedWatermark) {
        try {
            long startMs = System.currentTimeMillis();

            // Prepare request using the proper DTO
            WatermarkDetectionDTO request = new WatermarkDetectionDTO(originalWatermark,extractedWatermark);
            
            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WatermarkDetectionDTO> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling image service to detect watermark");
            
            // Make REST call to watermark detection endpoint
            ResponseEntity<WatermarkDetectionResponseDTO> response = restTemplate.exchange(
                imageServiceUrl + "/detect",
                HttpMethod.POST,
                entity, WatermarkDetectionResponseDTO.class);
            
            WatermarkDetectionResponseDTO responseBody = response.getBody();
            long elapsedMs = System.currentTimeMillis() - startMs;
            
            if (responseBody != null && responseBody.isSuccess()) {
                boolean detected = responseBody.isWatermarkDetected();
                
                // Log detailed detection results
                if (responseBody.getData() != null && responseBody.getData().getDetectionResult() != null) {
                    var detectionResult = responseBody.getData().getDetectionResult();
                    var metrics = responseBody.getData().getMetrics();
                    
                    log.info("Watermark detection completed - Match: {}, PCC: {:.4f}, Threshold: {:.2f}, MSE: {:.4f}, PSNR: {:.2f}, SSIM: {:.4f}", 
                        detected, 
                        metrics != null ? metrics.getPcc() : 0.0,
                        detectionResult.getPccThreshold(),
                        metrics != null ? metrics.getMse() : 0.0,
                        metrics != null ? metrics.getPsnr() : 0.0,
                        metrics != null ? metrics.getSsim() : 0.0);
                }
                // Persist JSON debug log
                saveDetectionLog(new HashMap<>() {{
                    put("success", true);
                    put("detected", detected);
                    put("elapsedMs", elapsedMs);
                    put("message", responseBody.getMessage());
                    Map<String, Object> requestInfo = new HashMap<>();
                    requestInfo.put("note", "order = detect(extracted, original) but service signature is (original, extracted)");
                    requestInfo.put("originalBase64", originalWatermark);
                    requestInfo.put("extractedBase64", extractedWatermark);
                    // include pccThreshold in request section for quick visibility
                    requestInfo.put("pccThreshold", request.getPccThreshold());
                    put("request", requestInfo);
                    put("fullResponseBody", responseBody);
                }});
                
                return detected;
            } else {
                log.warn("Watermark detection service returned unsuccessful response: {}", 
                    responseBody != null ? responseBody.getMessage() : "null");
                // Persist JSON debug log on unsuccessful
                saveDetectionLog(new HashMap<>() {{
                    put("success", false);
                    put("detected", false);
                    put("elapsedMs", elapsedMs);
                    put("message", responseBody != null ? responseBody.getMessage() : "null response");
                    Map<String, Object> requestInfo = new HashMap<>();
                    requestInfo.put("originalBase64", originalWatermark);
                    requestInfo.put("extractedBase64", extractedWatermark);
                    requestInfo.put("pccThreshold", null);
                    put("request", requestInfo);
                    put("fullResponseBody", responseBody);
                }});
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to call watermark detection service: {}", e.getMessage());
            // Persist error JSON debug log
            saveDetectionLog(new HashMap<>() {{
                put("success", false);
                put("detected", false);
                put("error", e.getMessage());
                Map<String, Object> requestInfo = new HashMap<>();
                requestInfo.put("originalBase64", originalWatermark);
                requestInfo.put("extractedBase64", extractedWatermark);
                requestInfo.put("pccThreshold", null);
                put("request", requestInfo);
            }});
            return false; // Return false on error to allow fallback behavior
        }
    }

    private void saveDetectionLog(Map<String, Object> logData) {
        try {
            Path dir = Path.of("debug_output");
            Files.createDirectories(dir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path file = dir.resolve(timestamp + "_detect_log.json");
            byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(logData);
            Files.write(file, jsonBytes);
        } catch (Exception ignored) {
        }
    }

    // Intentionally saving raw base64 as requested; no hashing/truncation helpers.
    
    /**
     * Upload image to Cloudinary and return the response
     * 
     * @param imageBase64 Base64 encoded image
     * @param publicId Optional public ID for the image
     * @return Cloudinary upload response
     */
    public WatermarkUploadResponseDTO uploadToCloudinary(String imageBase64, String publicId) {
        try {
            // Prepare request payload for Cloudinary upload
            WatermarkUploadDTO requestPayload = new WatermarkUploadDTO(imageBase64);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WatermarkUploadDTO> entity = new HttpEntity<>(requestPayload, headers);
            
            log.info("Uploading image to Cloudinary, public_id: {}", publicId);
            
            // Make REST call to Cloudinary upload endpoint
            ResponseEntity<WatermarkUploadResponseDTO> response = restTemplate.exchange(
                uploadServiceUrl + "/upload",
                HttpMethod.POST,
                entity,
                WatermarkUploadResponseDTO.class);
            
            WatermarkUploadResponseDTO responseBody = response.getBody();
            
            if (responseBody != null && responseBody.isSuccess()) {
                log.info("Image uploaded successfully to Cloudinary, public_id: {}", 
                    responseBody.getData() != null ? responseBody.getData().getPublicId() : "unknown");
                return responseBody;
            } else {
                log.warn("Cloudinary upload failed: {}", 
                    responseBody != null ? responseBody.getMessage() : "null response");
                throw new RuntimeException("Cloudinary upload failed: " + 
                    (responseBody != null ? responseBody.getMessage() : "unknown error"));
            }
            
        } catch (Exception e) {
            log.error("Failed to upload image to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }
}
