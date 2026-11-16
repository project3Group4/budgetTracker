package com.budgettracker.api.dto;

import com.budgettracker.api.entity.Expense;

import java.math.BigDecimal;

/**
 * Response class for expense entity
 * @param id Long expense id
 * @param financialId Long financial id
 * @param amount BigDecimal amount in expense
 * @param description String description
 */
public record ExpenseResponse(Long id, Long financialId, BigDecimal amount, String description) {
    /**
     * Creates a new expense response from expense
     * @param expense expense object
     * @return expense response
     */
    public static ExpenseResponse from(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getFinancial().getId(),
                expense.getAmount(),
                expense.getDescription()
        );
    }
}
