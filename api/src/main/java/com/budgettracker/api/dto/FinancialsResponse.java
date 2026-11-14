package com.budgettracker.api.dto;

import com.budgettracker.api.entity.Financials;

import java.math.BigDecimal;

public record FinancialsResponse(Long id, Long paycheckId, Long categoryId, BigDecimal setAmount) {
    public static FinancialsResponse from(Financials financials){
        return new FinancialsResponse(
                financials.getId(),
                financials.getPaycheck().getId(),
                financials.getCategory().getId(),
                financials.getSetAmount()
        );
    }
}
