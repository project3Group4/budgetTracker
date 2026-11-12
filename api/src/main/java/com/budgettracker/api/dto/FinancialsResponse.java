package com.budgettracker.api.dto;

import java.math.BigDecimal;

public record FinancialsResponse(
        Long financialId,
        Long paycheckId,
        Long categoryId,
        BigDecimal setAmount
) {}
