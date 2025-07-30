package com.example.TTECHT.service.impl;

import com.example.TTECHT.entity.watermark.Watermark;
import com.example.TTECHT.exception.AppException;
import com.example.TTECHT.exception.ErrorCode;
import com.example.TTECHT.repository.user.SellerRepository;
import org.springframework.stereotype.Service;

import com.example.TTECHT.repository.watermark.WatermarkRepository;

import com.example.TTECHT.dto.request.WatermarkCreationRequest;
import com.example.TTECHT.dto.request.WatermarkUpdateRequest;
import com.example.TTECHT.dto.repsonse.WatermarkResponse;
import com.example.TTECHT.service.WatermarkService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WatermarkServiceImpl implements WatermarkService {

    WatermarkRepository watermarkRepository;
    SellerRepository sellerRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('SELLER')")
    public WatermarkResponse createWatermark(WatermarkCreationRequest request) {

        try {
            log.info("Creating watermark for store: {}", request.getStoreName());

           
            // check if store exists
            sellerRepository.findByStoreName(request.getStoreName())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NAME_NOT_FOUND, "Store does not exist"));

            // check if the watermark already exists
            if (watermarkRepository.existsByStoreName((request.getStoreName()))) {
                throw new AppException(ErrorCode.WATERMARK_ALREADY_EXISTS, "Watermark already exists for this store");
            }

            // create and save the watermark
            Watermark watermark = Watermark.builder()
                .storeName(request.getStoreName())
                .watermarkUrlImage(request.getWatermarkUrlImage())
                .build();

            watermarkRepository.save(watermark);
            log.info("Watermark created successfully for store: {}", request.getStoreName());

            return WatermarkResponse.builder()
                .watermarkId(watermark.getWatermarkId())
                .storeName(watermark.getStoreName())
                .watermarkUrlImage(watermark.getWatermarkUrlImage())
                .build();


        } catch (AppException e) {
            log.error("Error creating watermark for store: {}", request.getStoreName(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating watermark for store: {}", request.getStoreName(), e);
            throw new AppException(ErrorCode.WATERMARK_CREATION_FAILED);
        }

    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SELLER')")
    public WatermarkResponse getWatermarkByStoreName(String storeName) {

        log.info("Retrieving watermark for store: {}", storeName);
        // check if store existss
        sellerRepository.findByStoreName(storeName)
            .orElseThrow(() -> new AppException(ErrorCode.STORE_NAME_NOT_FOUND, "Store does not exist"));

        // check if watermark exists
        Watermark watermark = watermarkRepository.findByStoreName(storeName)
            .orElseThrow(() -> new AppException(ErrorCode.WATERMARK_NOT_FOUND, "Watermark not found for this store"));
        log.info("Watermark retrieved successfully for store: {}", storeName);
        return WatermarkResponse.builder()
            .watermarkId(watermark.getWatermarkId())
            .storeName(watermark.getStoreName())
            .watermarkUrlImage(watermark.getWatermarkUrlImage())
            .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SELLER')")
    public WatermarkResponse updateWatermark(String storeName, WatermarkUpdateRequest request) {
        try {
            log.info("Updating watermark for store: {}", storeName);

            // check if store exists
            sellerRepository.findByStoreName(storeName)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NAME_NOT_FOUND, "Store does not exist"));

            // check if watermark exists
            Watermark existingWatermark = watermarkRepository.findByStoreName(storeName)
                .orElseThrow(() -> new AppException(ErrorCode.WATERMARK_NOT_FOUND, "Watermark not found for this store"));

            // update watermark details
            existingWatermark.setWatermarkUrlImage(request.getWatermarkUrlImage());
            watermarkRepository.save(existingWatermark);
            log.info("Watermark updated successfully for store: {}", storeName);

            return WatermarkResponse.builder()
                .watermarkId(existingWatermark.getWatermarkId())
                .storeName(existingWatermark.getStoreName())
                .watermarkUrlImage(existingWatermark.getWatermarkUrlImage())
                .build();

        } catch (AppException e) {
            log.error("Error updating watermark for store: {}", storeName, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating watermark for store: {}", storeName, e);
            throw new AppException(ErrorCode.WATERMARK_UPDATE_FAILED);
        }
    }



    @Override
    @Transactional
    @PreAuthorize("hasRole('SELLER')")
    public void deleteWatermark(String storeName) {
        try {
            log.info("Deleting watermark for store: {}", storeName);

            // check if store exists
            sellerRepository.findByStoreName(storeName)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND, "Store does not exist"));

            // check if watermark exists
            watermarkRepository.findByStoreName(storeName)
                .orElseThrow(() -> new AppException(ErrorCode.WATERMARK_NOT_FOUND, "Watermark does not exist for this store"));

            // delete watermark
            watermarkRepository.deleteByStoreName(storeName);
            log.info("Watermark deleted successfully for store: {}", storeName);

        } catch (AppException e) {
            log.error("Error deleting watermark for store: {}", storeName, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting watermark for store: {}", storeName, e);
            throw new AppException(ErrorCode.WATERMARK_DELETION_FAILED);
        }
    }
}
