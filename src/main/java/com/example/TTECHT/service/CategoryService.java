package com.example.TTECHT.service;

import com.example.TTECHT.dto.CategoryCreateDTO;
import com.example.TTECHT.dto.CategoryDTO;
import com.example.TTECHT.entity.Category;

import java.util.List;

public interface CategoryService {
    
    List<CategoryDTO> getAllCategories();
    
    CategoryDTO getCategoryById(Long id);
    
    CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO);
    
    CategoryDTO updateCategory(Long id, CategoryCreateDTO categoryCreateDTO);
    
    void deleteCategory(Long id);
    
    List<CategoryDTO> searchCategoriesByName(String name);
    
    CategoryDTO getCategoryByName(String name);
    
    boolean existsByName(String name);
    
    Category findEntityById(Long id);
    
    long getProductCountByCategory(Long categoryId);
}
