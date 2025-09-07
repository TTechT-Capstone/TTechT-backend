package com.example.TTECHT.entity.watermark;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "watermark_detection_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatermarkDetectionHistory {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "detection_id")
   private Long detectionId;

   @Column(name = "product_id", nullable = false)
   private Long productId;

   @Column(name = "store_name", nullable = false)
   private String storeName;

   @Column(name = "detected_image_base64", columnDefinition = "TEXT")
   private String detectedImageBase64;

   @Column(name = "detection_timestamp", nullable = false)
   private LocalDateTime detectionTimestamp;

   @Column(name = "watermark_id")
   private Long watermarkId;

   @Column(name = "detection_message")
   private String detectionMessage;

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(name = "watermark_detect_response", columnDefinition = "JSONB")
   private JsonNode watermarkDetectResponse;

   @Column(name = "detect_status")
   private String detectStatus;

   @Column(name = "watermark_base64", columnDefinition = "TEXT")
   private String watermarkBase64;
}
