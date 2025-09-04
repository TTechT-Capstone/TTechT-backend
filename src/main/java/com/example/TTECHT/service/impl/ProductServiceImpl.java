package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.ProductCreateDTO;
import com.example.TTECHT.dto.ProductDTO;
import com.example.TTECHT.entity.Category;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.entity.ProductColor;
import com.example.TTECHT.entity.ProductImage;
import com.example.TTECHT.entity.ProductSize;
import com.example.TTECHT.entity.user.User;
import com.example.TTECHT.repository.ProductColorRepository;
import com.example.TTECHT.repository.ProductImageRepository;
import com.example.TTECHT.repository.user.SellerRepository;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.ProductSizeRepository;
import com.example.TTECHT.repository.watermark.WatermarkRepository;
import com.example.TTECHT.entity.watermark.Watermark;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.CategoryService;
import com.example.TTECHT.service.ProductService;
import com.example.TTECHT.service.external.WatermarkService;
import com.example.TTECHT.dto.watermark.WatermarkEmbedResponseDTO;
import com.example.TTECHT.dto.watermark.WatermarkExtractDTO;
import com.example.TTECHT.dto.watermark.WatermarkExtractResponseDTO;
import com.example.TTECHT.dto.watermark.WatermarkUploadResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Base64;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.io.FileWriter;
import java.io.File;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductImageRepository productImageRepository;
    private final WatermarkRepository watermarkRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final WatermarkService watermarkService;
    private final SellerRepository sellerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        
        // Optimize by bulk loading colors, sizes, and images for all products in the page
        List<Long> productIds = products.getContent().stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return products.map(this::convertToDTO);
        }
        
        // Bulk load colors, sizes, and images for all products
        Map<Long, List<String>> colorsMap = getColorsMapByProductIds(productIds);
        Map<Long, List<String>> sizesMap = getSizesMapByProductIds(productIds);
        Map<Long, List<String>> imagesMap = getImagesMapByProductIds(productIds);
        
        return products.map(product -> convertToDTOWithPreloadedData(product, colorsMap, sizesMap, imagesMap));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = findEntityById(id);
        return convertToDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductCreateDTO productCreateDTO, String sellerUsername) {
        Category category = categoryService.findEntityById(productCreateDTO.getCategoryId());
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new RuntimeException("Seller not found: " + sellerUsername));

        Product product = new Product();
        product.setStoreName(productCreateDTO.getStoreName());
        product.setCategory(category);
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setPrice(productCreateDTO.getPrice());
        product.setStockQuantity(productCreateDTO.getStockQuantity());
        product.setBrand(productCreateDTO.getBrand());
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);
        
        // Save colors if provided
        if (productCreateDTO.getColors() != null && !productCreateDTO.getColors().isEmpty()) {
            List<ProductColor> colors = productCreateDTO.getColors().stream()
                    .map(colorName -> {
                        ProductColor color = new ProductColor();
                        color.setProduct(savedProduct);
                        color.setColor(colorName.trim());
                        return color;
                    })
                    .collect(Collectors.toList());
            productColorRepository.saveAll(colors);
        }
        
        // Save sizes if provided
        if (productCreateDTO.getSizes() != null && !productCreateDTO.getSizes().isEmpty()) {
            List<ProductSize> sizes = productCreateDTO.getSizes().stream()
                    .map(sizeName -> {
                        ProductSize size = new ProductSize();
                        size.setProduct(savedProduct);
                        size.setSize(sizeName.trim());
                        return size;
                    })
                    .collect(Collectors.toList());
            productSizeRepository.saveAll(sizes);
        }
        
        // Process images if provided (max 4 images)
        if (productCreateDTO.getImages() != null && !productCreateDTO.getImages().isEmpty()) {
            processProductImages(savedProduct, productCreateDTO.getImages(), productCreateDTO.getStoreName());
        }

        return convertToDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductCreateDTO productCreateDTO) {
        Product product = findEntityById(id);
        Category category = categoryService.findEntityById(productCreateDTO.getCategoryId());

        product.setStoreName(productCreateDTO.getStoreName());
        product.setCategory(category);
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setPrice(productCreateDTO.getPrice());
        product.setStockQuantity(productCreateDTO.getStockQuantity());
        product.setBrand(productCreateDTO.getBrand());

        Product updatedProduct = productRepository.save(product);
        
        // Update colors
        productColorRepository.deleteByProductId(id);
        if (productCreateDTO.getColors() != null && !productCreateDTO.getColors().isEmpty()) {
            List<ProductColor> colors = productCreateDTO.getColors().stream()
                    .map(colorName -> {
                        ProductColor color = new ProductColor();
                        color.setProduct(updatedProduct);
                        color.setColor(colorName.trim());
                        return color;
                    })
                    .collect(Collectors.toList());
            productColorRepository.saveAll(colors);
        }
        
        // Update sizes
        productSizeRepository.deleteByProductId(id);
        if (productCreateDTO.getSizes() != null && !productCreateDTO.getSizes().isEmpty()) {
            List<ProductSize> sizes = productCreateDTO.getSizes().stream()
                    .map(sizeName -> {
                        ProductSize size = new ProductSize();
                        size.setProduct(updatedProduct);
                        size.setSize(sizeName.trim());
                        return size;
                    })
                    .collect(Collectors.toList());
            productSizeRepository.saveAll(sizes);
        }
        
        // Update images
        productImageRepository.deleteByProductProductId(id);
        if (productCreateDTO.getImages() != null && !productCreateDTO.getImages().isEmpty()) {
            processProductImages(updatedProduct, productCreateDTO.getImages(), productCreateDTO.getStoreName());
        }

        return convertToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = findEntityById(id);
        // Colors and sizes will be deleted automatically due to cascade settings
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String name, Long categoryId, String storeName, Pageable pageable) {
        Page<Product> products = productRepository.findByFilters(name, categoryId, storeName, pageable);
        
        // Optimize by bulk loading colors, sizes, and images for all products in the page
        List<Long> productIds = products.getContent().stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return products.map(this::convertToDTO);
        }
        
        // Bulk load colors, sizes, and images for all products
        Map<Long, List<String>> colorsMap = getColorsMapByProductIds(productIds);
        Map<Long, List<String>> sizesMap = getSizesMapByProductIds(productIds);
        Map<Long, List<String>> imagesMap = getImagesMapByProductIds(productIds);
        
        return products.map(product -> convertToDTOWithPreloadedData(product, colorsMap, sizesMap, imagesMap));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByStore(String storeName) {
        return productRepository.findByStoreName(storeName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateStock(Long id, Integer newStock) {
        Product product = findEntityById(id);
        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductsWithFilters(String category, String search) {
        List<Product> products;

        if (category != null && search != null) {
            // Find category by name first
            try {
                Category categoryEntity = categoryService.findEntityById(Long.parseLong(category));
                products = productRepository.findByCategoryCategoryId(categoryEntity.getCategoryId())
                        .stream()
                        .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                // If category is not a number, try to find by name
                products = productRepository.findAll()
                        .stream()
                        .filter(p -> p.getCategory().getName().toLowerCase().contains(category.toLowerCase()))
                        .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (category != null) {
            try {
                Long categoryId = Long.parseLong(category);
                products = productRepository.findByCategoryCategoryId(categoryId);
            } catch (NumberFormatException e) {
                // If category is not a number, try to find by name
                products = productRepository.findAll()
                        .stream()
                        .filter(p -> p.getCategory().getName().toLowerCase().contains(category.toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (search != null) {
            products = productRepository.findByNameContaining(search);
        } else {
            products = productRepository.findAll();
        }

        return products.stream().map(this::convertToMap).collect(Collectors.toList());
    }

    private Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setStoreName(product.getStoreName());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        if (product.getSeller() != null) {
            dto.setSellerUsername(product.getSeller().getUsername());
            dto.setSellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName());
        }
        dto.setSoldQuantity(product.getSoldQuantity());
        dto.setBrand(product.getBrand());
        dto.setCreatedAt(product.getCreatedAt());
        
        // Load colors, sizes, and images
        dto.setColors(productColorRepository.findColorsByProductId(product.getProductId()));
        dto.setSizes(productSizeRepository.findSizesByProductId(product.getProductId()));
        dto.setImages(productImageRepository.findImageUrlsByProductId(product.getProductId()));
        
        return dto;
    }

    private Map<String, Object> convertToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", product.getProductId());
        map.put("storeName", product.getStoreName());
        map.put("categoryId", product.getCategory().getCategoryId());
        map.put("categoryName", product.getCategory().getName());
        map.put("name", product.getName());
        map.put("description", product.getDescription());
        map.put("price", product.getPrice());
        map.put("stockQuantity", product.getStockQuantity());
        map.put("brand", product.getBrand());
        map.put("createdAt", product.getCreatedAt());
        
        // Add colors, sizes, and images to map
        map.put("colors", productColorRepository.findColorsByProductId(product.getProductId()));
        map.put("sizes", productSizeRepository.findSizesByProductId(product.getProductId()));
        map.put("images", productImageRepository.findImageUrlsByProductId(product.getProductId()));
        
        return map;
    }

    private Map<String, Object> convertDTOToMap(ProductDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", dto.getProductId());
        map.put("storeName", dto.getStoreName());
        map.put("categoryId", dto.getCategoryId());
        map.put("categoryName", dto.getCategoryName());
        map.put("name", dto.getName());
        map.put("description", dto.getDescription());
        map.put("price", dto.getPrice());
        map.put("stockQuantity", dto.getStockQuantity());
        map.put("brand", dto.getBrand());
        map.put("createdAt", dto.getCreatedAt());
        map.put("colors", dto.getColors());
        map.put("sizes", dto.getSizes());
        map.put("images", dto.getImages());
        return map;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getBestSellerProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellerProducts(pageable)
                .stream()
                .map(this::convertToBestSellerDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getBestSellerProductsByCategory(Long categoryId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellerProductsByCategory(categoryId, pageable)
                .stream()
                .map(this::convertToBestSellerDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getTopSellingProducts(int minSoldQuantity, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findProductsWithMinimumSales(minSoldQuantity, pageable)
                .stream()
                .map(this::convertToBestSellerDTO)
                .collect(Collectors.toList());
    }
    
    private ProductDTO convertToBestSellerDTO(Product product) {
        ProductDTO dto = convertToDTO(product);
        dto.setSoldQuantity(product.getSoldQuantity());
        dto.setIsBestSeller(product.getSoldQuantity() > 0);
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getNewArrivalProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findNewArrivalProducts(pageable)
                .stream()
                .map(this::convertToNewArrivalDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getNewArrivalProductsByCategory(Long categoryId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findNewArrivalProductsByCategory(categoryId, pageable)
                .stream()
                .map(this::convertToNewArrivalDTO)
                .collect(Collectors.toList());
    }

    
    private ProductDTO convertToNewArrivalDTO(Product product) {
        ProductDTO dto = convertToDTO(product);
        // Mark as new arrival if created within last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        dto.setIsNewArrival(product.getCreatedAt().isAfter(thirtyDaysAgo));
        return dto;
    }
    
    /**
     * Process product images by calling watermark service and saving the resulting URLs
     * 
     * @param product The saved product entity
     * @param imageBase64List List of base64 encoded images (max 4)
     * @param storeName Store name for watermarking
     */
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processProductImages(Product product, List<String> imageBase64List, String storeName) {
        if (imageBase64List == null || imageBase64List.isEmpty()) {
            return;
        }
        
        // Validate max 4 images
        if (imageBase64List.size() > 4) {
            throw new RuntimeException("Maximum 4 images allowed per product");
        }
        
        try {
            // Ensure we're working with a managed Product entity
            Product managedProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + product.getProductId()));

            log.info("Starting image processing for store: {} with product ID: {}", storeName, managedProduct.getProductId());
            
            System.out.println("this is store name: " + storeName);
            // check if the store exists
            sellerRepository.findByStoreName(storeName).orElseThrow(() -> new RuntimeException("Store does not exist"));

            // Get watermark image for this store
            String watermarkImageUrl = watermarkRepository.findByStoreName(storeName)
                    .orElseThrow(() -> new RuntimeException("Watermark not found for store: " + storeName))
                    .getWatermarkUrlImage();
            
            // Convert URL to base64 if it's a URL, otherwise use as-is
            String watermarkImageBase64;
            if (watermarkImageUrl != null && (watermarkImageUrl.startsWith("http") || watermarkImageUrl.startsWith("@http"))) {
                watermarkImageBase64 = convertUrlToBase64(watermarkImageUrl);
            } else {
                watermarkImageBase64 = watermarkImageUrl; // Already base64 or other format
            }

            log.info("Successfully retrieved watermark for store: {}, length: {}", storeName, 
                watermarkImageBase64 != null ? watermarkImageBase64.length() : 0);
            
                        // Process each image individually to maintain unique jsonImage for each
        for (int i = 0; i < imageBase64List.size(); i++) {
            String imageBase64 = imageBase64List.get(i);
            
                log.info("Processing image {} of {}, length: {}", i + 1, imageBase64List.size(), 
                    imageBase64 != null ? imageBase64.length() : 0);
            
            if (imageBase64 == null || imageBase64.trim().isEmpty()) {
                    log.warn("Skipping empty image {}", i + 1);
                continue; // Skip empty images
            }
            
            try {
                    // Check if we have any existing product images to compare against
                    List<ProductImage> existingImages = productImageRepository.findAll();
                    log.info("Found {} existing images in database", existingImages.size());
                    
                                                if (existingImages.isEmpty()) {
                            // CASE 1: No existing images in database - proceed with direct embedding
                            log.info("No existing images in database, proceeding with direct watermark embedding for image {}", i + 1);
                            processNewImageWithEmbedding(managedProduct, imageBase64, watermarkImageBase64, i + 1);
                        } else {
                        // Filter out images that have valid jsonImage metadata
                        List<ProductImage> imagesWithMetadata = existingImages.stream()
                            .filter(img -> img.getJsonImage() != null && !img.getJsonImage().isNull())
                            .collect(Collectors.toList());
                        
                        log.info("Found {} images with valid metadata out of {} total images", 
                            imagesWithMetadata.size(), existingImages.size());
                        
                        if (imagesWithMetadata.isEmpty()) {
                            // CASE 1b: Images exist but all have empty/null jsonImage - treat as cold start
                            log.info("Found {} existing images but all have empty jsonImage metadata, treating as cold start for image {}", 
                                existingImages.size(), i + 1);
                            processNewImageWithEmbedding(managedProduct, imageBase64, watermarkImageBase64, i + 1);
                } else {
                            // CASE 2: Existing images with metadata found - try to find matching metadata
                            log.info("Found {} images with valid metadata, attempting to find match for image {}", 
                                imagesWithMetadata.size(), i + 1);
                            
                            boolean matchFound = findAndProcessWithMatchingMetadata(managedProduct, imageBase64, watermarkImageBase64, imagesWithMetadata, i + 1);
                            
                            if (!matchFound) {
                                // No matching metadata found, treat as new image type
                                log.info("No matching metadata found among {} valid metadata entries for image {}, proceeding with new watermark embedding", 
                                    imagesWithMetadata.size(), i + 1);
                                processNewImageWithEmbedding(managedProduct, imageBase64, watermarkImageBase64, i + 1);
                            }
                        }
                }
                
            } catch (Exception e) {
                log.error("Error processing image {} for product {}", i + 1, managedProduct.getProductId(), e);
                    throw new RuntimeException("Failed to process image " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            log.info("Successfully processed {} images for product {}", imageBase64List.size(), managedProduct.getProductId());
            
        } catch (Exception e) {
            log.error("Error getting watermark for store: {}", storeName, e);
            throw new RuntimeException("Failed to get watermark for store: " + storeName, e);
                }
    }
    
    /**
     * Process new image with watermark embedding (for cold start scenarios)
     */
    private void processNewImageWithEmbedding(Product product, String imageBase64, String watermarkImageBase64, int imageIndex) {
        try {
            log.info("Starting watermark embedding for image {} with watermark length: {}", 
                imageIndex, watermarkImageBase64 != null ? watermarkImageBase64.length() : 0);
            
            WatermarkEmbedResponseDTO watermarkResponse = watermarkService.embedWatermark(
                imageBase64, watermarkImageBase64, 0.6);
            
            log.info("Watermark embedding completed for image {}, response success: {}", 
                imageIndex, watermarkResponse != null ? watermarkResponse.isSuccess() : "null");
            
            saveWatermarkedImage(product, watermarkResponse, imageIndex);
        } catch (Exception e) {
            log.error("Error in embedding watermark for new image {}: {}", imageIndex, e.getMessage(), e);
            throw new RuntimeException("Failed to embed watermark for image " + imageIndex + ": " + e.getMessage());
        }
    }
    
    /**
     * Save watermarked image to database
     */
    private void saveWatermarkedImage(Product product, WatermarkEmbedResponseDTO watermarkResponse, int imageIndex) {
        if (watermarkResponse.isSuccess() && watermarkResponse.getData() != null) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product); // Product is already managed
            
            // Extract the watermarked image base64 from the response data
            String watermarkedImageBase64 = null;
            if (watermarkResponse.getData().has("watermarked_image")) {
                watermarkedImageBase64 = watermarkResponse.getData().get("watermarked_image").asText();
            } else {
                throw new RuntimeException("No watermarked_image found in response for image " + imageIndex);
            }
            
            // Call upload to cloudinary with the watermarked image
            WatermarkUploadResponseDTO uploadResponse = watermarkService.uploadToCloudinary(watermarkedImageBase64, "product_image_" + product.getProductId() + "_" + imageIndex);
            if (uploadResponse.isSuccess() && uploadResponse.getData() != null) {
                productImage.setUrlImage(uploadResponse.getData().getUrl());
            } else {
                throw new RuntimeException("Failed to upload image to Cloudinary for image " + imageIndex);
            }
            
            // Save only the metadata part of the watermark response - this contains the essential watermarking info
            JsonNode metadataNode = watermarkResponse.getData().get("metadata");
            if (metadataNode != null) {
                productImage.setJsonImage(metadataNode);
            } else {
                throw new RuntimeException("No metadata found in watermark response for image " + imageIndex);
            }
            
            productImageRepository.save(productImage);
            log.info("Successfully processed and watermarked image {} for product {}", imageIndex, product.getProductId());
        } else {
            log.warn("Watermark service failed for image {}: {}", imageIndex, watermarkResponse.getMessage());
            throw new RuntimeException("Failed to process image " + imageIndex + ": " + watermarkResponse.getMessage());
        }
    }
    
    /**
     * Find matching metadata and process image with extraction + watermark detection
     * This solves Problem 2: When we have existing images, we need to find the right metadata
     */
    private boolean findAndProcessWithMatchingMetadata(Product product, String newImageBase64, 
                                                     String watermarkImageBase64, List<ProductImage> existingImages, 
                                                     int imageIndex) {
        try {
            // Strategy: Try to extract watermark from the new image using metadata from existing images
            // If extraction succeeds and matches a known watermark, we found the right metadata
            
            for (ProductImage existingImage : existingImages) {
                if (existingImage.getJsonImage() == null) {
                    continue; // Skip images without metadata
                }
                
                try {

                    System.out.println("this is watermark image base64: " + watermarkImageBase64);
                    System.out.println("this is new image base64: " + newImageBase64);
                    System.out.println("this is existing image json: " + existingImage.getJsonImage());
                    
                    // Try to extract watermark using this existing image's metadata
                    WatermarkExtractResponseDTO extractResponse = watermarkService.extractWatermark(
                        newImageBase64, existingImage.getJsonImage());

                    System.out.println("den day r nha");
                    
                    if (extractResponse != null && extractResponse.isSuccess() && 
                        extractResponse.getData() != null && 
                        extractResponse.getData().getExtractedWatermark() != null) {
                        // Extraction successful - check if this watermark exists in our database
                        String extractedWatermark = extractResponse.getData().getExtractedWatermark(); // This would be the extracted watermark

                        // Save extract response and extracted watermark to separate JSON files for debugging
                        saveToJsonFile(extractResponse, "extract_response.json", "Extract Response");
                        saveToJsonFile(extractedWatermark, "extracted_watermark.txt", "Extracted Watermark");
                        
                        log.info("Extraction successful for image {} using metadata from image ID {}", 
                            imageIndex, existingImage.getImageId());
                        
                        // Check if watermark is detected in the database
                        boolean watermarkDetected = checkWatermarkInDatabase(extractedWatermark);
                        
                        if (watermarkDetected) {
                            // Found matching metadata and detected watermark!
                            log.info("Found matching metadata and detected watermark for image {} using existing image ID {}", 
                                imageIndex, existingImage.getImageId());
                            
                            // Skip embedding and save - watermark already exists, no need to process
                            log.info("Skipping image {} - watermark already detected, no embedding needed", imageIndex);
                            return true; // Match found, skip processing
                        } else {
                            // No watermark detected - proceed with embedding and save
                            log.info("No watermark detected for image {} using metadata from image ID {}, proceeding with embedding", 
                                imageIndex, existingImage.getImageId());
                            
                            // Process with embedding using the existing metadata
                            processImageWithEmbeddingAndExistingMetadata(product, newImageBase64, watermarkImageBase64, existingImage.getJsonImage(), imageIndex);
                            return true; // Processed successfully
                        }
                    }
                } catch (Exception e) {
                    // Extraction failed with this metadata, try next one
                    log.debug("Extraction failed with metadata from image {}: {}", existingImage.getImageId(), e.getMessage());
                    continue;
                }
            }
            
            return false; // No matching metadata found
        } catch (Exception e) {
            log.error("Error in finding matching metadata for image {}: {}", imageIndex, e.getMessage());
            return false;
        }
    }
    
    /**
     * Save image with existing metadata when a match is found
     */
    private void saveImageWithExistingMetadata(Product product, String imageBase64, JsonNode existingMetadata, int imageIndex) {
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product); // Product is already managed
        productImage.setUrlImage(imageBase64); // Store the original image
        productImage.setJsonImage(existingMetadata); // Reuse the existing metadata
        
        productImageRepository.save(productImage);
        log.info("Saved image {} with existing metadata for product {}", imageIndex, product.getProductId());
    }
    
    /**
     * Process image with embedding when no watermark is detected but we have existing metadata
     * This will embed a new watermark and save the result
     */
    private void processImageWithEmbeddingAndExistingMetadata(Product product, String imageBase64, String watermarkImageBase64, JsonNode existingMetadata, int imageIndex) {
        try {
            log.info("Starting watermark embedding for image {} (no watermark detected, using existing metadata)", imageIndex);
            
            // Embed watermark using the existing metadata as reference
            WatermarkEmbedResponseDTO watermarkResponse = watermarkService.embedWatermark(
                imageBase64, watermarkImageBase64, 0.6);
            
            log.info("Watermark embedding completed for image {}, response success: {}", 
                imageIndex, watermarkResponse != null ? watermarkResponse.isSuccess() : "null");
            
            if (watermarkResponse != null && watermarkResponse.isSuccess() && watermarkResponse.getData() != null) {
                ProductImage productImage = new ProductImage();
                productImage.setProduct(product); // Product is already managed
                
                // Extract the watermarked image base64 from the response data
                String watermarkedImageBase64 = null;
                if (watermarkResponse.getData().has("watermarked_image")) {
                    watermarkedImageBase64 = watermarkResponse.getData().get("watermarked_image").asText();
                } else {
                    throw new RuntimeException("No watermarked_image found in response for image " + imageIndex);
                }
                
                // Call upload to cloudinary with the watermarked image
                WatermarkUploadResponseDTO uploadResponse = watermarkService.uploadToCloudinary(watermarkedImageBase64, "product_image_" + product.getProductId() + "_" + imageIndex);
                if (uploadResponse.isSuccess() && uploadResponse.getData() != null) {
                    productImage.setUrlImage(uploadResponse.getData().getUrl());
                } else {
                    throw new RuntimeException("Failed to upload image to Cloudinary for image " + imageIndex);
                }
                
                // Save the new metadata from the embedding process
                JsonNode newMetadataNode = watermarkResponse.getData().get("metadata");
                if (newMetadataNode != null) {
                    productImage.setJsonImage(newMetadataNode);
                } else {
                    throw new RuntimeException("No metadata found in watermark response for image " + imageIndex);
                }
                
                productImageRepository.save(productImage);
                log.info("Successfully processed and watermarked image {} for product {} (no existing watermark detected)", imageIndex, product.getProductId());
            } else {
                log.warn("Watermark service failed for image {}: {}", imageIndex, watermarkResponse.getMessage());
                throw new RuntimeException("Failed to process image " + imageIndex + ": " + watermarkResponse.getMessage());
            }
        } catch (Exception e) {
            log.error("Error in embedding watermark for image {} (no watermark detected): {}", imageIndex, e.getMessage(), e);
            throw new RuntimeException("Failed to embed watermark for image " + imageIndex + ": " + e.getMessage());
        }
    }
    
    /**
     * Helper method to convert image URL to base64 format
     * @param imageUrl The URL of the image (e.g., Cloudinary URL)
     * @return Base64 encoded string of the image
     * @throws RuntimeException if the image cannot be downloaded or converted
     */
    public String convertUrlToBase64(String imageUrl) {
        try {
            log.info("Converting image URL to base64: {}", imageUrl);
            
            // Remove any leading/trailing whitespace and @ symbol if present
            String cleanUrl = imageUrl.trim();
            if (cleanUrl.startsWith("@")) {
                cleanUrl = cleanUrl.substring(1);
            }
            
            // Download the image from URL
            URL url = new URL(cleanUrl);
            try (InputStream inputStream = url.openStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                
                // Convert to base64
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                
                // Determine the image format from URL extension
                String imageFormat = "png"; // default
                if (cleanUrl.toLowerCase().contains(".jpg") || cleanUrl.toLowerCase().contains(".jpeg")) {
                    imageFormat = "jpeg";
                } else if (cleanUrl.toLowerCase().contains(".png")) {
                    imageFormat = "png";
                } else if (cleanUrl.toLowerCase().contains(".gif")) {
                    imageFormat = "gif";
                } else if (cleanUrl.toLowerCase().contains(".webp")) {
                    imageFormat = "webp";
                }
                
                // Add data URL prefix
                String dataUrl = "data:image/" + imageFormat + ";base64," + base64Image;
                
                log.info("Successfully converted image URL to base64, original length: {}, base64 length: {}", 
                    imageBytes.length, dataUrl.length());
                
                return dataUrl;
            }
            
        } catch (IOException e) {
            log.error("Failed to download and convert image from URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to convert image URL to base64: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error converting image URL to base64: {}", imageUrl, e);
            throw new RuntimeException("Failed to process image URL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Utility method to save data to JSON files for debugging
     * @param data The data to save
     * @param filename The filename to save to
     * @param description Description of what the data represents
     */
    private void saveToJsonFile(Object data, String filename, String description) {
        try {
            // Create debug directory if it doesn't exist
            String debugDir = "debug_output";
            File directory = new File(debugDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate timestamp for unique filenames
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFilename = debugDir + File.separator + timestamp + "_" + filename;
            
            // Convert data to JSON string
            String jsonContent;
            if (data instanceof String) {
                jsonContent = data.toString();
            } else {
                jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            }
            
            // Write to file
            try (FileWriter writer = new FileWriter(fullFilename)) {
                writer.write(jsonContent);
            }
            
            log.info("Saved {} to file: {}", description, fullFilename);
            
        } catch (Exception e) {
            log.error("Failed to save {} to file: {}", description, e.getMessage());
        }
    }
    
    /**
     * Check if watermark exists in database by calling watermark detection service
     * Loops through all watermarks in the database and calls detectWatermark API
     */
    private boolean checkWatermarkInDatabase(String extractedWatermark) {
        try {
            System.out.println("extracted watermark: " + extractedWatermark);
            log.info("Starting checkWatermarkInDatabase with extracted watermark length: {}", 
                extractedWatermark != null ? extractedWatermark.length() : 0);
            
            // Get all watermarks from the database
             List<Watermark> allWatermarks = watermarkRepository.findAll();

            if (allWatermarks.isEmpty()) {
                log.warn("No watermarks found in database for comparison");
                return false;
            }
            
            log.info("Checking extracted watermark against {} watermarks in database", allWatermarks.size());
            
            // Loop through each watermark and call the detection service
            for (Watermark watermark : allWatermarks) {
                try {
                    // Get the watermark image for comparison
                    String watermarkImageUrl = watermark.getWatermarkUrlImage();
                    
                    if (watermarkImageUrl != null && !watermarkImageUrl.trim().isEmpty()) {
                        // Convert URL to base64 if it's a URL, otherwise use as-is
                        String watermarkImageBase64;
                        if (watermarkImageUrl.startsWith("http") || watermarkImageUrl.startsWith("@http")) {
                            watermarkImageBase64 = convertUrlToBase64(watermarkImageUrl);
                        } else {
                            watermarkImageBase64 = watermarkImageUrl; // Already base64 or other format
                        }
                        System.out.println("watermark image base64: " + watermarkImageBase64);
                        // Call the watermark detection service
                        log.info("Calling detectWatermark for watermark ID: {}, extracted watermark length: {}, original watermark length: {}", 
                            watermark.getWatermarkId(), 
                            extractedWatermark != null ? extractedWatermark.length() : 0,
                            watermarkImageBase64 != null ? watermarkImageBase64.length() : 0);
                        
                        boolean detected = watermarkService.detectWatermark(watermarkImageBase64, extractedWatermark);
                        
                        log.info("Detection result for watermark ID {}: {}", watermark.getWatermarkId(), detected);
                        
                        if (detected) {
                            log.info("Watermark detected! Matches watermark ID: {}", watermark.getWatermarkId());
                            return true; // Found a match, no need to check further
                        }
                    } else {
                        log.debug("Skipping watermark ID {} - no watermark image available", watermark.getWatermarkId());
                    }
                } catch (Exception e) {
                    log.warn("Error checking watermark ID {}: {}", watermark.getWatermarkId(), e.getMessage());
                    // Continue with next watermark instead of failing completely
                    continue;
                }
            }
            
            log.debug("No matching watermark found after checking all {} watermarks", allWatermarks.size());
            return false; // No matches found
            
        } catch (Exception e) {
            log.error("Error in watermark database check: {}", e.getMessage());
            return false; // Return false on error to allow fallback behavior
        }
    }
 
    
    /**
     * Bulk load colors for multiple products to avoid N+1 queries
     */
    private Map<Long, List<String>> getColorsMapByProductIds(List<Long> productIds) {
        List<ProductColor> allColors = productColorRepository.findByProductProductIdIn(productIds);
        return allColors.stream()
                .collect(Collectors.groupingBy(
                    color -> color.getProduct().getProductId(),
                    Collectors.mapping(ProductColor::getColor, Collectors.toList())
                ));
    }
    
    /**
     * Bulk load sizes for multiple products to avoid N+1 queries
     */
    private Map<Long, List<String>> getSizesMapByProductIds(List<Long> productIds) {
        List<ProductSize> allSizes = productSizeRepository.findByProductProductIdIn(productIds);
        return allSizes.stream()
                .collect(Collectors.groupingBy(
                    size -> size.getProduct().getProductId(),
                    Collectors.mapping(ProductSize::getSize, Collectors.toList())
                ));
    }
    
    /**
     * Bulk load images for multiple products to avoid N+1 queries
     */
    private Map<Long, List<String>> getImagesMapByProductIds(List<Long> productIds) {
        List<ProductImage> allImages = productImageRepository.findByProductProductIdIn(productIds);
        return allImages.stream()
                .collect(Collectors.groupingBy(
                    image -> image.getProduct().getProductId(),
                    Collectors.mapping(ProductImage::getUrlImage, Collectors.toList())
                ));
    }
    
    /**
     * Convert Product to DTO with preloaded colors, sizes, and images data
     */
    private ProductDTO convertToDTOWithPreloadedData(Product product, 
                                                    Map<Long, List<String>> colorsMap,
                                                    Map<Long, List<String>> sizesMap,
                                                    Map<Long, List<String>> imagesMap) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setStoreName(product.getStoreName());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        if (product.getSeller() != null) {
            dto.setSellerUsername(product.getSeller().getUsername());
            dto.setSellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName());
        }
        dto.setSoldQuantity(product.getSoldQuantity());
        dto.setBrand(product.getBrand());
        dto.setCreatedAt(product.getCreatedAt());
        
        // Use preloaded data instead of making individual queries
        dto.setColors(colorsMap.getOrDefault(product.getProductId(), List.of()));
        dto.setSizes(sizesMap.getOrDefault(product.getProductId(), List.of()));
        dto.setImages(imagesMap.getOrDefault(product.getProductId(), List.of()));
        
        return dto;
    }
}
