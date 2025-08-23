package com.example.TTECHT.controller;

import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.Category;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.CategoryRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Test Controller for Image Processing
 * Tests the processProductImages method from ProductServiceImpl
 */
@RestController
@RequestMapping("/api/test/image-processing")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ImageProcessingTestController {

    private final ProductServiceImpl productService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Test image processing using processProductImages method
     * Creates a temporary Product entity and processes images
     */
    @PostMapping("/process-images")
    public ResponseEntity<Map<String, Object>> testImageProcessing(
            @RequestBody Map<String, Object> request) {
        
        try {
            List<String> images = (List<String>) request.get("images");
            String storeName = (String) request.get("store_name");
            
            if (images == null || images.isEmpty() || storeName == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required fields: images (array) and store_name"
                ));
            }
            
            log.info("Testing image processing for store: {} with {} images", storeName, images.size());
            
            // Create a temporary Product entity in the database for testing
            Product tempProduct = createTemporaryProduct(storeName);
            
            // Call the actual processProductImages method
            productService.processProductImages(tempProduct, images, storeName);
            
            // Clean up: Delete the temporary product and its images after testing
            productRepository.delete(tempProduct);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Image processing completed successfully");
            result.put("store_name", storeName);
            result.put("images_processed", images.size());
            result.put("test_type", "processProductImages_test");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in image processing test: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error in image processing test: " + e.getMessage(),
                "test_type", "processProductImages_test"
            ));
        }
    }
    
    /**
     * Create a temporary product in the database for testing
     */
    private Product createTemporaryProduct(String storeName) {
        try {
            // Get the first available category (or create a default one if needed)
            List<Category> categories = categoryRepository.findAll();
            Category category = categories.isEmpty() ? createDefaultCategory() : categories.get(0);
            
            // Get the first available user (or create a default one if needed)
            List<User> users = userRepository.findAll();
            User user = users.isEmpty() ? createDefaultUser() : users.get(0);
            
            // Create temporary product
            Product tempProduct = new Product();
            tempProduct.setStoreName(storeName);
            tempProduct.setCategory(category);
            tempProduct.setName("TEMP_TEST_PRODUCT_" + System.currentTimeMillis());
            tempProduct.setDescription("Temporary product for image processing test");
            tempProduct.setPrice(new BigDecimal("0.00"));
            tempProduct.setStockQuantity(0);
            tempProduct.setBrand("TEST_BRAND");
            tempProduct.setSeller(user);
            tempProduct.setSoldQuantity(0);
            tempProduct.setCreatedAt(LocalDateTime.now());
            
            // Save to database to get a real ID
            Product savedProduct = productRepository.save(tempProduct);
            log.info("Created temporary product with ID: {} for testing", savedProduct.getProductId());
            
            return savedProduct;
            
        } catch (Exception e) {
            log.error("Error creating temporary product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create temporary product: " + e.getMessage());
        }
    }
    
    /**
     * Create a default category if none exists
     */
    private Category createDefaultCategory() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Default category for testing");
        return categoryRepository.save(category);
    }
    
    /**
     * Create a default user if none exists
     */
    private User createDefaultUser() {
        User user = new User();
        user.setUsername("test_user_" + System.currentTimeMillis());
        user.setEmail("test@example.com");
        user.setPassword("test_password");
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }
}
