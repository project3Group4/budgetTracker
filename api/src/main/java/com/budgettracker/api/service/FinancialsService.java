package com.budgettracker.api.service;

import com.budgettracker.api.dto.ExpenseResponse;
import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.Expense;
import com.budgettracker.api.entity.Financials;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.ExpenseRepository;
import com.budgettracker.api.repository.FinancialsRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialsService{
    private final FinancialsRepository financialsRepository;
    private final ExpenseRepository expenseRepository;
    private final PaycheckService paycheckService;
    private final CategoryService categoryService;

    public FinancialsService(FinancialsRepository financialsRepository, ExpenseRepository expenseRepository, PaycheckService paycheckService,
                             CategoryService categoryService){
        this.financialsRepository = financialsRepository;
        this.expenseRepository = expenseRepository;
        this.paycheckService = paycheckService;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    //retrieves all financial allocations saved w/ authenticated user's paycheck
    public List<FinancialsResponse> getFinancials(Long userId) {
        Paycheck paycheck = paycheckService.findPaycheck(userId);

        List<Financials> financials = financialsRepository.findByPaycheckId(paycheck.getId());

        List<FinancialsResponse> responses = new ArrayList<>();

        for (Financials financial : financials) {
            responses.add(new FinancialsResponse(financial.getId(), financial.getPaycheck().getId(),
                    financial.getCategory().getId(), financial.getSetAmount()));
        }

        return responses;
    }

    /**
     * Finds a specific financial allocation saved with the authenticated user's paycheck and financial ID
     * @param financialId the ID of the financial item we're looking for
     * @param userId the authenticated user's ID
     * @return response with financial information
     */
    @Transactional(readOnly = true)
    public FinancialsResponse findFinancialByID(Long financialId, Long userId) {
        return FinancialsResponse.from(findOwned(financialId, userId));
    }

    public void deleteFinancial(Long financialId, Long userId) {
        Financials financial = findOwned(financialId, userId);

        financialsRepository.delete(financial);
    }

    /**
     * Finds a specific financial allocation saved with the authenticated user's paycheck and financial ID and then returns the expenses
     * @param financialId the ID of the financial item we're looking for
     * @param userId the authenticated user's ID
     * @return response with list of expense information
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> findFinancialByIDReturnExpenses(Long financialId, Long userId) {
        Financials financial = findOwned(financialId, userId);

        List<Expense> expenses = expenseRepository.findByFinancialId(financial.getId());

        List<ExpenseResponse> responses = new ArrayList<>();

        for (Expense expense : expenses) {
            responses.add(new ExpenseResponse(expense.getId(), expense.getFinancial().getId(), expense.getAmount(),
                    expense.getDescription()));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findExpenseByID(Long financialId, Long expenseId, Long userId) {
        Financials financial = findOwned(financialId, userId);
        Expense expense = expenseRepository
                .findByIdAndFinancialId(expenseId, financial.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));
        return ExpenseResponse.from(expense);
    }

    public void deleteExpense(Long financialId, Long expenseId, Long userId){
        Financials financial = findOwned(financialId, userId);
        Expense expense = expenseRepository
                .findByIdAndFinancialId(expenseId, financial.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));

        expenseRepository.delete(expense);
    }

    @Transactional
    //saves or updates an individual category's budget allocation
    public Financials saveFinancial(Paycheck paycheck, FinancialsRequest request){
        Category category = categoryService.getAvailableCategory(request.categoryId());

        Financials financial = financialsRepository.findByPaycheckIdAndCategoryId(paycheck.getId(),
                category.getId()).orElse(null);

        if(financial == null){
            financial = new Financials(paycheck, category, request.setAmount());
        }else{
            financial.updateSetAmount(request.setAmount());
        }

        return financialsRepository.save(financial);
    }


    private Financials findOwned(Long financialId, Long userId){
        Paycheck paycheck = paycheckService.findPaycheck(userId);

        return financialsRepository
                .findByIdAndPaycheckId(financialId, paycheck.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));
    }
}