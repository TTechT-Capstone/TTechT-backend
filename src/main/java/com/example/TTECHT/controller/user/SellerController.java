package com.example.TTECHT.controller.user;

import com.example.TTECHT.dto.repsonse.SellerResponse;
import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.SellerCreationRequest;
import com.example.TTECHT.service.SellerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerController {

    SellerService sellerService;

    @PostMapping
    public ApiResponse<SellerResponse> createSeller(@RequestBody @Valid SellerCreationRequest request) {
        log.info("Creating seller for user ID: {}", request.getUserId());

        return ApiResponse.<SellerResponse>builder()
                .result(sellerService.createSeller(request))
                .build();
    }

    @GetMapping("/{sellerId}")
    public ApiResponse<SellerResponse> getSeller(@PathVariable Long sellerId) {
        return ApiResponse.<SellerResponse>builder()
                .result(sellerService.getSeller(sellerId))
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<SellerResponse> getSellerByUserId(@PathVariable Long userId) {
        return ApiResponse.<SellerResponse>builder()
                .result(sellerService.getSellerByUserId(userId))
                .build();
    }

    @GetMapping("/my-profile/{userId}")
    public ApiResponse<SellerResponse> getMySellerProfile(@PathVariable String userId) {
         return ApiResponse.<SellerResponse>builder()
                 .result(sellerService.getMySellerProfile(userId))
                 .build();
    }

    @GetMapping
    public ApiResponse<List<SellerResponse>> getAllSellers() {
        return ApiResponse.<List<SellerResponse>>builder()
                .result(sellerService.getAllSellers())
                .build();
    }
}