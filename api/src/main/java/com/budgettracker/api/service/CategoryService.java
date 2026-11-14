package com.budgettracker.api.service;

import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for category controller
 */
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Constructor for class
     * @param categoryRepository category repository class
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * retrieves categories available to authenticated user, converts each category entity into category
     * @param pageable Pageable
     * @param activeOnly Boolean
     * @return response dto, returns list of response objects
     */
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(Pageable pageable, boolean activeOnly) {
        Page<Category> categories;

        if (activeOnly) {
            categories = categoryRepository.findByIsActiveTrue(pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        return categories.map(category -> {
            Long createdByUserId = null;

            if (category.getCreatedByUser() != null) {
                createdByUserId = category.getCreatedByUser().getId();
            }

            return new CategoryResponse(category.getId(), category.getName(), category.isActive(),
                    createdByUserId);
        });
    }

    /**
     * Gets category from id
     * @param categoryId Long
     * @return category
     */
    @Transactional(readOnly = true)
    public Category getAvailableCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (!category.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is inactive");
        }

        return category;
    }
}