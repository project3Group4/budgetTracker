package com.budgettracker.api.controller;

import com.budgettracker.api.dto.CategoryCreate;
import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.dto.UpdateCategory;
import com.budgettracker.api.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Handles HTTP requests for categories.
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Creates the category controller.
     *
     * @param categoryService category service
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Gets a paginated list of categories.
     *
     * @param page page number
     * @param size number of categories per page
     * @param activeOnly whether to return only active categories
     * @return paginated category responses
     */
    @GetMapping
    public PagedModel<CategoryResponse> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid page or size"
            );
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by("id")
        );

        return new PagedModel<>(
                categoryService.getCategories(pageable, activeOnly)
        );
    }

    /**
     * Creates a category for an administrator.
     *
     * @param jwt authenticated user's JWT
     * @param request category creation data
     * @return created category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CategoryCreate request) {

        CategoryResponse response =
                categoryService.createCategory(userId(jwt), request);

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{categoryId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /**
     * Gets a category by ID.
     *
     * @param categoryId category ID
     * @return category response
     */
    @GetMapping("/{categoryId}")
    public CategoryResponse getCategory(@PathVariable Long categoryId) {
        return categoryService.getCategory(categoryId);
    }

    /**
     * Updates a category's active status.
     *
     * @param jwt authenticated user's JWT
     * @param categoryId category ID
     * @param request updated category data
     * @return updated category
     */
    @PatchMapping("/{categoryId}")
    public CategoryResponse updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategory request) {

        return categoryService.updateCategory(
                userId(jwt),
                categoryId,
                request
        );
    }

    /**
     * Deactivates a category while preserving existing financial records.
     *
     * @param jwt authenticated user's JWT
     * @param categoryId category ID
     */
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long categoryId) {

        categoryService.deleteCategory(userId(jwt), categoryId);
    }

    /**
     * Gets the user ID from the JWT subject.
     *
     * @param jwt authenticated user's JWT
     * @return authenticated user's ID
     */
    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}