package com.budgettracker.api.dto;

import java.math.BigDecimal;

public record PaycheckResponse(
        Long id,
        BigDecimal paycheckAmount,
        BigDecimal savingsPercent
) {}
