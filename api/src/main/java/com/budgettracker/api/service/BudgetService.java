package com.budgettracker.api.service;

import com.budgettracker.api.dto.BudgetRequest;
import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.entity.Paycheck;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

/**
 * Service for budget controller
 */
@Service
public class BudgetService {
    private final PaycheckService paycheckService;
    private final FinancialsService financialsService;

    /**
     * Constructor for class
     * @param paycheckService paycheck service class
     * @param financialsService financial service class
     */
    public BudgetService(PaycheckService paycheckService, FinancialsService financialsService) {
        this.paycheckService = paycheckService;
        this.financialsService = financialsService;
    }

    /**
     * Saves the new budget
     * @param userId Long
     * @param request BudgetRequest object
     */
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

        Paycheck paycheck = paycheckService.savePaycheck(userId, request.paycheck());

        //loops through each category allocation submitted by the frontend and calls method is financials service
        for (FinancialsRequest financial : request.financials()) {
            financialsService.saveFinancial(paycheck, financial);
        }
    }
}