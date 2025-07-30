package com.example.TTECHT.controller.watermark;

import com.example.TTECHT.dto.request.WatermarkCreationRequest;
import com.example.TTECHT.dto.request.WatermarkUpdateRequest;
import com.example.TTECHT.dto.repsonse.WatermarkResponse;
import com.example.TTECHT.service.WatermarkService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/watermark")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WatermarkController {
    private final WatermarkService watermarkService;

    @PostMapping
    public ResponseEntity<WatermarkResponse> createWatermark(@RequestBody WatermarkCreationRequest request) {
        return ResponseEntity.ok(watermarkService.createWatermark(request));
    }

    @GetMapping("/{storeName}")
    public ResponseEntity<WatermarkResponse> getWatermark(@PathVariable String storeName) {
        return ResponseEntity.ok(watermarkService.getWatermarkByStoreName(storeName));
    }

    @PutMapping("/{storeName}")
    public ResponseEntity<WatermarkResponse> updateWatermark(@PathVariable String storeName, @RequestBody WatermarkUpdateRequest request) {
        return ResponseEntity.ok(watermarkService.updateWatermark(storeName, request));
    }

    @DeleteMapping("/{storeName}")
    public ResponseEntity<Void> deleteWatermark(@PathVariable String storeName) {
        watermarkService.deleteWatermark(storeName);
        return ResponseEntity.noContent().build();
    }
}