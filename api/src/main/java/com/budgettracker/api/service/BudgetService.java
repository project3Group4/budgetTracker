package com.budgettracker.api.service;

import com.budgettracker.api.dto.BudgetRequest;
import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.entity.Paycheck;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BudgetService {
    private final PaycheckService paycheckService;
    private final FinancialsService financialsService;

    public BudgetService(PaycheckService paycheckService, FinancialsService financialsService) {
        this.paycheckService = paycheckService;
        this.financialsService = financialsService;
    }

    @Transactional
    public void saveBudget(Long userId, BudgetRequest request) {
        //hashset only allows unique values meaning there wont be duplicate categories allowed
        Set<Long> categoryIds = new HashSet<>();

        for (FinancialsRequest financial : request.financials()) {
            if (financial == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Financial allocation cannot be null");
            }
            if (!categoryIds.add(financial.categoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate category");
            }
        }
        //validate before saving so backend will reject request before modifying db
        validateBudgetAmounts(request);
        Paycheck paycheck = paycheckService.savePaycheck(userId, request.paycheck());

        //loops through each category allocation submitted by the frontend and calls method is financials service
        for (FinancialsRequest financial : request.financials()) {
            financialsService.saveFinancial(paycheck, financial);
        }
    }

    private void validateBudgetAmounts(BudgetRequest request) {
        BigDecimal paycheckAmount = request.paycheck().paycheckAmount();
        BigDecimal savingsPercent = request.paycheck().savingsPercent();
        BigDecimal savingsAmount = paycheckAmount.multiply(savingsPercent).divide(new BigDecimal("100"),
                2, RoundingMode.HALF_UP);

        BigDecimal availableAmount = paycheckAmount.subtract(savingsAmount);
        BigDecimal totalAllocations = BigDecimal.ZERO;

        for (FinancialsRequest financial : request.financials()) {
            totalAllocations = totalAllocations.add(financial.setAmount());
        }

        if (totalAllocations.compareTo(availableAmount) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total budget allocations cannot " +
                    "exceed available paycheck amount after savings");
        }
    }
}