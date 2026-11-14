package com.budgettracker.api.dto;

import com.budgettracker.api.entity.Expense;

import java.math.BigDecimal;

public record ExpenseResponse(Long id, Long financialId, BigDecimal amount, String description) {
    public static ExpenseResponse from(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getFinancial().getId(),
                expense.getAmount(),
                expense.getDescription()
        );
    }
}
