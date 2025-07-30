package com.example.TTECHT.service.impl;

import com.example.TTECHT.constant.PredefinedRole;
import com.example.TTECHT.dto.repsonse.SellerResponse;
import com.example.TTECHT.dto.request.SellerCreationRequest;
import com.example.TTECHT.dto.request.SellerUpdateRequest;
import com.example.TTECHT.entity.user.Role;
import com.example.TTECHT.entity.user.Seller;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.exception.AppException;
import com.example.TTECHT.exception.ErrorCode;
import com.example.TTECHT.mapper.SellerMapper;
import com.example.TTECHT.repository.user.RoleRepository;
import com.example.TTECHT.repository.user.SellerRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerServiceImpl implements SellerService {
    SellerRepository sellerRepository;
    UserRepository userRepository;
    SellerMapper sellerMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    @Transactional
    public SellerResponse createSeller(SellerCreationRequest request) {
        HashSet<Role> roles = new HashSet<>();

        roleRepository.findByName(PredefinedRole.SELLER_ROLE).ifPresent(roles::add);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .isActive(true)
                .build();


        if (sellerRepository.existsByStoreName(request.getStoreName())) {
            log.warn("Store name '{}' already exists", request.getStoreName());
            throw new AppException(ErrorCode.STORE_NAME_ALREADY_EXISTS);
        }

        userRepository.save(user);

        // Create seller
        Seller seller = Seller.builder()
                .user(user)
                .storeName(request.getStoreName())
                .storeDescription(request.getStoreDescription())
                .build();

        Seller savedSeller = sellerRepository.save(seller);
        log.info("Successfully created seller with ID: {}", savedSeller.getId());

        return sellerMapper.toSellerResponse(savedSeller);
    }

    public SellerResponse getSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        return sellerMapper.toSellerResponse(seller);
    }

    @Transactional
    // check???
//    @PostAuthorize("returnObject.userId == authentication.principal.userId or hasRole('ADMIN')")
    public SellerResponse updateSeller(Long sellerId, SellerUpdateRequest request) {
        log.info("Updating seller with ID: {}", sellerId);
        log.info("Update request: {}", request);

        try {

            Seller seller = sellerRepository.findByUserId(Long.valueOf(sellerId))
                    .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

            if (request.getUsername() != null &&
                    !request.getUsername().equals(seller.getUser().getUsername()) &&
                    userRepository.existsByUsername(request.getUsername())) {

                log.warn("Username '{}' already exists", request.getUsername());
                throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }

            if (request.getEmail() != null &&
                    !request.getEmail().equals(seller.getUser().getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {

                log.warn("Email '{}' already exists", request.getEmail());
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }

            if (request.getStoreName() != null &&
                    !request.getStoreName().equals(seller.getStoreName()) &&
                    sellerRepository.existsByStoreName(request.getStoreName())) {

                log.warn("Store name '{}' already exists", request.getStoreName());
                throw new AppException(ErrorCode.STORE_NAME_ALREADY_EXISTS);
            }

            sellerMapper.updateSeller(seller, request);

            Seller updatedSeller = sellerRepository.save(seller);
            log.info("Successfully updated seller with ID: {}", updatedSeller.getId());

            return sellerMapper.toSellerResponse(updatedSeller);

        } catch (AppException e) {
            log.error("Application error updating seller: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating seller with ID: {}", sellerId, e);
            throw new AppException(ErrorCode.SELLER_UPDATE_FAILED);
        }
    }

    @Transactional
    // check again
//    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    public SellerResponse updateSellerByUserId(Long userId, SellerUpdateRequest request) {
        log.info("Updating seller for user ID: {}", userId);
        log.info("Update request: {}", request);

        try {

            if (!userRepository.existsById(String.valueOf(userId))) {
                log.warn("User with ID {} not found", userId);
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }

            Seller seller = sellerRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.warn("User with ID {} is not a seller", userId);
                        return new AppException(ErrorCode.SELLER_NOT_FOUND);
                    });


            if (request.getStoreName() != null &&
                    !request.getStoreName().equals(seller.getStoreName()) &&
                    sellerRepository.existsByStoreNameExcludingUserId(request.getStoreName(), userId)) {

                log.warn("Store name '{}' already exists", request.getStoreName());
                throw new AppException(ErrorCode.STORE_NAME_ALREADY_EXISTS);
            }

            sellerMapper.updateSeller(seller, request);

            Seller updatedSeller = sellerRepository.save(seller);
            log.info("Successfully updated seller for user ID: {}", userId);

            return sellerMapper.toSellerResponse(updatedSeller);

        } catch (AppException e) {
            log.error("Application error updating seller for user ID {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating seller for user ID: {}", userId, e);
            throw new AppException(ErrorCode.SELLER_UPDATE_FAILED);
        }
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
