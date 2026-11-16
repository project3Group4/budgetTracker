package com.budgettracker.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

/**
 * Request class for financials entity
 * @param categoryId Long category id
 * @param setAmount BigDecimal amount set for financial (min 0)
 */
public record FinancialsRequest(
        @NotNull
        @Positive
        Long categoryId,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal setAmount
) {}