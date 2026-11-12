package com.budgettracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoryResponse(
        Long id,
        String name,

        @JsonProperty("is_active")
        boolean isActive,

        Long createdByUserId
) {}
