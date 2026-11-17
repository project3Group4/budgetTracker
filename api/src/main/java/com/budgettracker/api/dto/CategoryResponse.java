package com.budgettracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body representing a category.
 *
 * @param id category ID
 * @param name category name
 * @param isActive whether the category is active
 * @param createdByUserId ID of the user who created the category
 */
public record CategoryResponse(
        Long id,
        String name,

        @JsonProperty("is_active")
        boolean isActive,

        Long createdByUserId
) {
}