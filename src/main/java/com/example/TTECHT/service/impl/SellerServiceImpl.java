package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.repsonse.SellerResponse;
import com.example.TTECHT.dto.request.SellerCreationRequest;
import com.example.TTECHT.entity.user.Seller;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.exception.AppException;
import com.example.TTECHT.exception.ErrorCode;
import com.example.TTECHT.mapper.SellerMapper;
import com.example.TTECHT.repository.user.SellerRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerServiceImpl implements SellerService {
    SellerRepository sellerRepository;
    UserRepository userRepository;
    SellerMapper sellerMapper;

    @Transactional
    public SellerResponse createSeller(SellerCreationRequest request) {
        log.info("Creating seller for user ID: {}", request.getUserId());

        // 1. Validate user exists
        User user = userRepository.findById(request.getUserId().toString())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Check if user is already a seller
        if (sellerRepository.existsByUserId(request.getUserId())) {
            log.warn("User {} is already a seller", request.getUserId());
            throw new AppException(ErrorCode.USER_ALREADY_SELLER);
        }

        // 3. Check if store name already exists
        if (sellerRepository.existsByStoreName(request.getStoreName())) {
            log.warn("Store name '{}' already exists", request.getStoreName());
            throw new AppException(ErrorCode.STORE_NAME_ALREADY_EXISTS);
        }

        // 4. Create seller entity
        Seller seller = sellerMapper.toSeller(request);
        seller.setUser(user);

        // 5. Save seller
        Seller savedSeller = sellerRepository.save(seller);
        log.info("Successfully created seller with ID: {}", savedSeller.getId());

        return sellerMapper.toSellerResponse(savedSeller);
    }

    public SellerResponse getSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        return sellerMapper.toSellerResponse(seller);
    }

    public SellerResponse getSellerByUserId(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        return sellerMapper.toSellerResponse(seller);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<SellerResponse> getAllSellers() {
        return sellerRepository.findAll()
                .stream()
                .map(sellerMapper::toSellerResponse)
                .toList();
    }


    // check again
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public SellerResponse getMySellerProfile(String userId) {
        return getSellerByUserId(Long.valueOf(userId));
    }
}
