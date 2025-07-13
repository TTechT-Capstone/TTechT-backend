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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Product Controller with integrated authentication and authorization
 * 
 * Access Control:
 * - GET operations: Public access (anyone can view products)
 * - POST operations: SELLER or ADMIN roles required
 * - PUT/PATCH/DELETE operations: Product owner (SELLER) or ADMIN required
 */

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
            @RequestParam(defaultValue = "1") int page,
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
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductCreateDTO productCreateDTO,
            Authentication authentication) {
        ProductDTO createdProduct = productService.createProduct(productCreateDTO, authentication.getName());
        return ResponseEntity.status(201).body(createdProduct);
    }

    /**
     * 4. PUT /api/products/{id} - Update existing product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER'))")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateDTO productCreateDTO,
            Authentication authentication) {

        ProductDTO updatedProduct = productService.updateProduct(id, productCreateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 5. DELETE /api/products/{id} - Delete product
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER'))")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable Long id, 
            Authentication authentication) {
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
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER'))")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stockUpdate,
            Authentication authentication) {

        ProductDTO updatedProduct = productService.updateStock(id, stockUpdate);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 11. GET /api/products/best-sellers - Get best selling products
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<List<ProductDTO>> getBestSellerProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductDTO> bestSellers = productService.getBestSellerProducts(limit);
        return ResponseEntity.ok(bestSellers);
    }
    
    /**
     * 12. GET /api/products/best-sellers/category/{categoryId} - Get best sellers by category
     */
    @GetMapping("/best-sellers/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getBestSellersByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductDTO> bestSellers = productService.getBestSellerProductsByCategory(categoryId, limit);
        return ResponseEntity.ok(bestSellers);
    }
    
    /**
     * 13. GET /api/products/new-arrivals - Get new arrival products
     */
    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductDTO>> getNewArrivalProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductDTO> newArrivals = productService.getNewArrivalProducts(limit);
        return ResponseEntity.ok(newArrivals);
    }


//    @GetMapping("/top-selling")
//    public ResponseEntity<List<ProductDTO>> getTopSellingProducts(
//            @RequestParam(defaultValue = "5") int minSoldQuantity,
//            @RequestParam(defaultValue = "20") int limit) {
//        List<ProductDTO> topSelling = productService.getTopSellingProducts(minSoldQuantity, limit);
//        return ResponseEntity.ok(topSelling);
//    }

}
