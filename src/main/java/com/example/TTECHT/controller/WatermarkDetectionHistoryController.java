package com.example.TTECHT.controller;

import com.example.TTECHT.entity.watermark.WatermarkDetectionHistory;
import com.example.TTECHT.repository.watermark.WatermarkDetectionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Watermark Detection History Controller
 * 
 * Provides APIs for managing and viewing watermark detection history records.
 * All endpoints require ADMIN role for security.
 */
@RestController
@RequestMapping("/api/watermark-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WatermarkDetectionHistoryController {

    private final WatermarkDetectionHistoryRepository watermarkDetectionHistoryRepository;

    /**
     * GET /api/watermark-history - Get all watermark detection history
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WatermarkDetectionHistory>> getAllWatermarkDetectionHistory() {
        try {
            List<WatermarkDetectionHistory> history = watermarkDetectionHistoryRepository.findAll();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/watermark-history/paginated - Get watermark detection history with pagination
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<WatermarkDetectionHistory>> getWatermarkDetectionHistoryPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "detectionTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WatermarkDetectionHistory> history = watermarkDetectionHistoryRepository.findAll(pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/watermark-history/{id} - Get specific watermark detection history by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WatermarkDetectionHistory> getWatermarkDetectionHistoryById(@PathVariable Long id) {
        try {
            return watermarkDetectionHistoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/watermark-history/product/{productId} - Get watermark detection history for specific product
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WatermarkDetectionHistory>> getWatermarkDetectionHistoryByProduct(
            @PathVariable Long productId) {
        try {
            List<WatermarkDetectionHistory> history = watermarkDetectionHistoryRepository.findByProductId(productId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/watermark-history/store/{storeName} - Get watermark detection history for specific store
     */
    @GetMapping("/store/{storeName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WatermarkDetectionHistory>> getWatermarkDetectionHistoryByStore(
            @PathVariable String storeName) {
        try {
            List<WatermarkDetectionHistory> history = watermarkDetectionHistoryRepository.findByStoreName(storeName);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/watermark-history/product/{productId}/store/{storeName} - Get watermark detection history for specific product and store
     */
    @GetMapping("/product/{productId}/store/{storeName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WatermarkDetectionHistory>> getWatermarkDetectionHistoryByProductAndStore(
            @PathVariable Long productId,
            @PathVariable String storeName) {
        try {
            List<WatermarkDetectionHistory> history = watermarkDetectionHistoryRepository.findByProductIdAndStoreName(productId, storeName);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * DELETE /api/watermark-history/{id} - Delete specific watermark detection history record
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWatermarkDetectionHistory(@PathVariable Long id) {
        try {
            if (watermarkDetectionHistoryRepository.existsById(id)) {
                watermarkDetectionHistoryRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/watermark-history/count - Get total count of watermark detection records
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getWatermarkDetectionHistoryCount() {
        try {
            long count = watermarkDetectionHistoryRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
