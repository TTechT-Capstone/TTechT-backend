package com.example.TTECHT.entity.watermark;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "watermarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Watermark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watermark_id")
    Long watermarkId;

    @Column(name = "store_name", nullable = true, unique = true)
    String storeName;

    @Column(name = "watermark_url_image", nullable = true)
    String watermarkUrlImage;
}
