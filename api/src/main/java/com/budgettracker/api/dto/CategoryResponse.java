package com.budgettracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response class for category entity
 * @param id Long category id
 * @param name String category name
 * @param isActive Boolean is/not active
 * @param createdByUserId Long id of user who created it
 */
public record CategoryResponse(
        Long id,
        String name,

        @JsonProperty("is_active")
        boolean isActive,

        Long createdByUserId
) {}
