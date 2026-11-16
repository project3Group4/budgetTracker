package com.budgettracker.api.dto;

import java.math.BigDecimal;

/**
 * Response class for paycheck entity
 * @param id Long paycheck id
 * @param paycheckAmount BigDecimal paycheck amount
 * @param savingsPercent BigDecimal savings percentage
 */
public record PaycheckResponse(
        Long id,
        BigDecimal paycheckAmount,
        BigDecimal savingsPercent
) {}
