package com.example.TTECHT.controller;

import com.example.TTECHT.dto.CategoryCreateDTO;
import com.example.TTECHT.dto.CategoryDTO;
import com.example.TTECHT.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 1. GET /api/categories - Get all categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 2. GET /api/categories/{id} - Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * 3. POST /api/categories - Create new category
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryCreateDTO);
        return ResponseEntity.status(201).body(createdCategory);
    }
    
    /**
     * 4. PUT /api/categories/{id} - Update existing category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryCreateDTO);
        return ResponseEntity.ok(updatedCategory);
    }
    
    /**
     * 5. DELETE /api/categories/{id} - Delete category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        response.put("deletedId", id);
        
        return ResponseEntity.ok(response);
    }






    /**
     * GET /api/categories/search - Search categories by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategoriesByName(@RequestParam String name) {
        List<CategoryDTO> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * GET /api/categories/name/{name} - Get category by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByName(@PathVariable String name) {
        CategoryDTO category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }
    
    /**
     * GET /api/categories/{id}/product-count - Get product count for category
     */
    @GetMapping("/{id}/product-count")
    public ResponseEntity<Map<String, Object>> getProductCountByCategory(@PathVariable Long id) {
        long productCount = categoryService.getProductCountByCategory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("categoryId", id);
        response.put("productCount", productCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/categories/exists/{name} - Check if category exists by name
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<Map<String, Object>> checkCategoryExists(@PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
}
