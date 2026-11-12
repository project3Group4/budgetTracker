package com.budgettracker.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;

public record PaycheckRequest(

        @NotNull
        @DecimalMin("0.01")
        BigDecimal paycheckAmount,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal savingsPercent

) {}