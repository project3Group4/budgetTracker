package com.budgettracker.api.dto;

import com.budgettracker.api.entity.Financials;

import java.math.BigDecimal;

/**
 * Response class for financials entity
 * @param id Long financial id
 * @param paycheckId Long paycheck id
 * @param categoryId Long category id
 * @param setAmount BigDecimal amount set for financial
 */
public record FinancialsResponse(Long id, Long paycheckId, Long categoryId, BigDecimal setAmount) {
    /**
     * Creates a new financials response from financials
     * @param financials financials object
     * @return financials response
     */
    public static FinancialsResponse from(Financials financials){
        return new FinancialsResponse(
                financials.getId(),
                financials.getPaycheck().getId(),
                financials.getCategory().getId(),
                financials.getSetAmount()
        );
    }
}
