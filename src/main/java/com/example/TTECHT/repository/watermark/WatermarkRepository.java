package com.example.TTECHT.repository.watermark;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TTECHT.entity.watermark.Watermark;

@Repository
public interface WatermarkRepository extends JpaRepository<Watermark, Long> {
    Optional<Watermark> findByStoreName(String storeName);

    boolean existsByStoreName(String storeName);

    void deleteByStoreName(String storeName);
}
