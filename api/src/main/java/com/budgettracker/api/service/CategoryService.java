package com.budgettracker.api.service;

import com.budgettracker.api.dto.CategoryCreate;
import com.budgettracker.api.dto.CategoryResponse;
import com.budgettracker.api.dto.UpdateCategory;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.Role;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.CategoryRepository;
import com.budgettracker.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Provides business logic for categories.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Creates the category service.
     *
     * @param categoryRepository category repository
     * @param userRepository user repository
     */
    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves categories with pagination.
     *
     * @param pageable pagination information
     * @param activeOnly whether to return only active categories
     * @return page of category responses
     */
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(
            Pageable pageable,
            boolean activeOnly) {

        Page<Category> categories = activeOnly
                ? categoryRepository.findByIsActiveTrue(pageable)
                : categoryRepository.findAll(pageable);

        return categories.map(this::toResponse);
    }

    /**
     * Finds an active category for use by another feature.
     *
     * @param categoryId category ID
     * @return active category
     * @throws ResponseStatusException if the category does not exist or is inactive
     */
    @Transactional(readOnly = true)
    public Category getAvailableCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));

        if (!category.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category is inactive"
            );
        }

        return category;
    }

    /**
     * Retrieves a category by ID.
     *
     * @param categoryId category ID
     * @return category response
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long categoryId) {
        return toResponse(findCategory(categoryId));
    }

    /**
     * Creates a category for an administrator.
     *
     * @param adminUserId administrator's user ID
     * @param request category creation data
     * @return created category
     */
    @Transactional
    public CategoryResponse createCategory(
            Long adminUserId,
            CategoryCreate request) {

        User admin = requireAdmin(adminUserId);

        Category category = new Category(
                admin,
                request.name().trim(),
                request.isActive()
        );

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Updates a category's active status.
     *
     * @param adminUserId administrator's user ID
     * @param categoryId category ID
     * @param request updated category data
     * @return updated category
     */
    @Transactional
    public CategoryResponse updateCategory(
            Long adminUserId,
            Long categoryId,
            UpdateCategory request) {

        requireAdmin(adminUserId);

        Category category = findCategory(categoryId);
        category.updateActive(request.isActive());

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Deactivates a category instead of permanently deleting it.
     * This preserves financial and expense records that reference the category.
     *
     * @param adminUserId administrator's user ID
     * @param categoryId category ID
     */
    @Transactional
    public void deleteCategory(Long adminUserId, Long categoryId) {

        requireAdmin(adminUserId);

        Category category = findCategory(categoryId);

        //soft delete: preserve category and mark it inactive
        category.updateActive(false);

        categoryRepository.delete(category);
    }

    /**
     * Finds a category or throws a not-found exception.
     *
     * @param categoryId category ID
     * @return matching category
     */
    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));
    }

    /**
     * Confirms that a user is an administrator.
     *
     * @param userId user ID
     * @return administrator user
     */
    private User requireAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin access required"
            );
        }

        return user;
    }

    /**
     * Converts a category entity into a response DTO.
     *
     * @param category category entity
     * @return category response
     */
    private CategoryResponse toResponse(Category category) {

        Long createdByUserId = category.getCreatedByUser() == null
                ? null
                : category.getCreatedByUser().getId();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isActive(),
                createdByUserId
        );
    }
}