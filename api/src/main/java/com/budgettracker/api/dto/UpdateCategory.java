package com.budgettracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body used to update a category's active status.
 *
 * @param isActive the new active status
 */
public record UpdateCategory(
        @NotNull(message = "is_active is required")
        @JsonProperty("is_active")
        Boolean isActive
) {
}