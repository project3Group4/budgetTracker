package com.budgettracker.api.dto;

import java.math.BigDecimal;

public record FinancialsResponse(Long id, Long paycheckId, Long categoryId, BigDecimal setAmount) {
}
