package com.budgettracker.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

public record FinancialsRequest(

        @NotNull
        @Positive
        Long categoryId,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal setAmount

) {}