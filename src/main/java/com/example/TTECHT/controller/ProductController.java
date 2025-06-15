package com.example.TTECHT.controller;

import com.example.TTECHT.dto.ProductCreateDTO;
import com.example.TTECHT.dto.ProductDTO;
import com.example.TTECHT.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure as needed for your frontend
public class ProductController {

    private final ProductService productService;

    /**
     * 1. GET /api/products - Get all products with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDTO> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(products);
    }


    /**
     * 2. GET /api/products/{id} - Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * 3. POST /api/products - Create new product
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO) {
        ProductDTO createdProduct = productService.createProduct(productCreateDTO);
        return ResponseEntity.status(201).body(createdProduct);
    }

    /**
     * 4. PUT /api/products/{id} - Update existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateDTO productCreateDTO) {

        ProductDTO updatedProduct = productService.updateProduct(id, productCreateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 5. DELETE /api/products/{id} - Delete product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        response.put("deletedId", id);

        return ResponseEntity.ok(response);
    }

    /**
     * 6. GET /api/products/category/{categoryId} - Get products by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * 7. GET /api/products/search - Search products by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@RequestParam String name) {
        List<ProductDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }


    /**
     * 8. GET /api/products/price-range - Get products by price range (/api/products/price-range?minPrice=10&maxPrice=20)
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        List<ProductDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }


    /**
     * 9. GET /api/products/store/{storeName} - Get products by store name
     */
    @GetMapping("/store/{storeName}")
    public ResponseEntity<List<ProductDTO>> getProductsByStore(@PathVariable String storeName) {
        List<ProductDTO> products = productService.getProductsByStore(storeName);
        return ResponseEntity.ok(products);
    }

    /**
     * 10. PATCH /api/products/{id}/stock - Update product stock quantity
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stockUpdate) {

        ProductDTO updatedProduct = productService.updateStock(id, stockUpdate);
        return ResponseEntity.ok(updatedProduct);
    }

}
