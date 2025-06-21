package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.SellerResponse;
import com.example.TTECHT.dto.request.SellerCreationRequest;

import java.util.List;

public interface SellerService {
    SellerResponse createSeller(SellerCreationRequest request);
    SellerResponse getSeller(Long sellerId);
    SellerResponse getSellerByUserId(Long userId);
    List<SellerResponse> getAllSellers();
    SellerResponse getMySellerProfile(String userId);
}
