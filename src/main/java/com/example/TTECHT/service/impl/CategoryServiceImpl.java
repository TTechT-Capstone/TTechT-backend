package com.example.TTECHT.service.impl;

import com.example.TTECHT.dto.CategoryCreateDTO;
import com.example.TTECHT.dto.CategoryDTO;
import com.example.TTECHT.entity.Category;
import com.example.TTECHT.repository.CategoryRepository;
import com.example.TTECHT.repository.ProductRepository;
import com.example.TTECHT.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = findEntityById(id);
        return convertToDTO(category);
    }
    
    @Override
    public CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO) {
        if (existsByName(categoryCreateDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryCreateDTO.getName() + "' already exists");
        }
        
        Category category = new Category();
        category.setName(categoryCreateDTO.getName());
        category.setDescription(categoryCreateDTO.getDescription());
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    @Override
    public CategoryDTO updateCategory(Long id, CategoryCreateDTO categoryCreateDTO) {
        Category category = findEntityById(id);
        
        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(categoryCreateDTO.getName()) && 
            existsByName(categoryCreateDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryCreateDTO.getName() + "' already exists");
        }
        
        category.setName(categoryCreateDTO.getName());
        category.setDescription(categoryCreateDTO.getDescription());
        
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }
    
    @Override
    public void deleteCategory(Long id) {
        Category category = findEntityById(id);
        
        // Check if category has products
        long productCount = getProductCountByCategory(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category. It has " + productCount + " products associated with it.");
        }
        
        categoryRepository.delete(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
        return convertToDTO(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getProductCountByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
