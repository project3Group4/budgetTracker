package com.budgettracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body used to create a category.
 *
 * @param name category name
 * @param isActive whether the category is active
 */
public record CategoryCreate(
        @NotBlank(message = "Category name cannot be blank")
        String name,

        @NotNull(message = "is_active is required")
        @JsonProperty("is_active")
        Boolean isActive
) {
}