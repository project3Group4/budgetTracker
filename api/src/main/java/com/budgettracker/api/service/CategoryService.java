package com.budgettracker.api.service;

import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    //retrieves categories available to authenticated user, converts each category entity into category
    //response dto, returns list of response objects
    public List<CategoryResponse> getCategories() {
        List<Category> categories =
                categoryRepository.findByIsActiveTrue();

        List<CategoryResponse> responses = new ArrayList<>();

        for (Category category : categories) {

            Long createdByUserId = null;

            if (category.getCreatedByUser() != null) {
                createdByUserId = category.getCreatedByUser().getId();
            }

            responses.add(new CategoryResponse(category.getId(), category.getName(), category.isActive(),
                    createdByUserId));
        }

        return responses;
    }

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