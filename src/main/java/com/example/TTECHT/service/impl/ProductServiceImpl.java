package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.ProductCreateDTO;
import com.example.TTECHT.dto.ProductDTO;
import com.example.TTECHT.entity.Category;
import com.example.TTECHT.entity.Product;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.service.CategoryService;
import com.example.TTECHT.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = findEntityById(id);
        return convertToDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductCreateDTO productCreateDTO) {
        Category category = categoryService.findEntityById(productCreateDTO.getCategoryId());

        Product product = new Product();
        product.setStoreName(productCreateDTO.getStoreName());
        product.setCategory(category);
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setPrice(productCreateDTO.getPrice());
        product.setStockQuantity(productCreateDTO.getStockQuantity());

        Product savedProduct = productRepository.save(product);
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

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = findEntityById(id);
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
        return productRepository.findByFilters(name, categoryId, storeName, pageable)
                .map(this::convertToDTO);
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
        dto.setCreatedAt(product.getCreatedAt());
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
        map.put("createdAt", product.getCreatedAt());
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
        map.put("createdAt", dto.getCreatedAt());
        return map;
    }
}
