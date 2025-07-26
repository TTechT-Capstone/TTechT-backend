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
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.repository.ProductSizeRepository;
import com.example.TTECHT.repository.user.UserRepository;
import com.example.TTECHT.service.CategoryService;
import com.example.TTECHT.service.ProductService;
import com.example.TTECHT.service.external.WatermarkService;
import com.example.TTECHT.dto.watermark.WatermarkResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final WatermarkService watermarkService;

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
    private void processProductImages(Product product, List<String> imageBase64List, String storeName) {
        if (imageBase64List == null || imageBase64List.isEmpty()) {
            return;
        }
        
        // Validate max 4 images
        if (imageBase64List.size() > 4) {
            throw new RuntimeException("Maximum 4 images allowed per product");
        }
        
        for (int i = 0; i < imageBase64List.size(); i++) {
            String imageBase64 = imageBase64List.get(i);
            
            if (imageBase64 == null || imageBase64.trim().isEmpty()) {
                continue; // Skip empty images
            }
            
            try {
                // Call watermark service for each image
//                WatermarkResponseDTO watermarkResponse = watermarkService.addWatermark(imageBase64, storeName);
                WatermarkResponseDTO watermarkResponse = watermarkService.addWatermark(imageBase64, storeName);
                if (watermarkResponse.isSuccess() && watermarkResponse.getImageUrl() != null) {
                    // Save the image URL to database
                    ProductImage productImage = new ProductImage();
                    productImage.setProduct(product);
                    productImage.setUrlImage(watermarkResponse.getImageUrl());
                    productImageRepository.save(productImage);
                    
                    log.info("Successfully processed image {} for product {}", i + 1, product.getProductId());
                } else {
                    log.warn("Watermark service failed for image {} of product {}: {}", 
                        i + 1, product.getProductId(), watermarkResponse.getMessage());
                    throw new RuntimeException("Failed to process image " + (i + 1) + ": " + watermarkResponse.getMessage());
                }
                
            } catch (Exception e) {
                log.error("Error processing image {} for product {}", i + 1, product.getProductId(), e);
                throw new RuntimeException("Failed to process image " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        
        log.info("Successfully processed {} images for product {}", imageBase64List.size(), product.getProductId());
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
