package com.budgettracker.api.controller;

import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.service.CategoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * The controller for the category API routes
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Constructor for controller
     * @param categoryService the service class
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Gets a paged model of the categories
     * @param page int for the page number
     * @param size int for how many items on the page
     * @param activeOnly boolean for active categories
     * @return a paged model of categories
     */
    @GetMapping
    public PagedModel<CategoryResponse> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size");
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id"));

        return new PagedModel<>(categoryService.getCategories(pageable, activeOnly));
    }
}