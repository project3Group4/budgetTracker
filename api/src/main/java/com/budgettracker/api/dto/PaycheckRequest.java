package com.budgettracker.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/**
 * Request entity for paycheck entity
 * @param paycheckAmount BigDecimal paycheck amount (min 0)
 * @param savingsPercent BigDecimal savings percentage (between 0 and 100)
 */
public record PaycheckRequest(
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal paycheckAmount,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal savingsPercent
) {}