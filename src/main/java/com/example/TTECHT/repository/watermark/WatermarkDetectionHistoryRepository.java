package com.example.TTECHT.repository.watermark;

import com.example.TTECHT.entity.watermark.WatermarkDetectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatermarkDetectionHistoryRepository extends JpaRepository<WatermarkDetectionHistory, Long> {

   List<WatermarkDetectionHistory> findByProductId(Long productId);

   List<WatermarkDetectionHistory> findByStoreName(String storeName);

   List<WatermarkDetectionHistory> findByProductIdAndStoreName(Long productId, String storeName);
}
