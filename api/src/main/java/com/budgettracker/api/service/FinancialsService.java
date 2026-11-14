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

/**
 * Service for financials controller
 */
@Service
public class FinancialsService{
    private final FinancialsRepository financialsRepository;
    private final ExpenseRepository expenseRepository;
    private final PaycheckService paycheckService;
    private final CategoryService categoryService;

    /**
     * Constructor for class
     * @param financialsRepository financials repository class
     * @param expenseRepository expense repository class
     * @param paycheckService paycheck service class
     * @param categoryService category service class
     */
    public FinancialsService(FinancialsRepository financialsRepository, ExpenseRepository expenseRepository, PaycheckService paycheckService,
                             CategoryService categoryService){
        this.financialsRepository = financialsRepository;
        this.expenseRepository = expenseRepository;
        this.paycheckService = paycheckService;
        this.categoryService = categoryService;
    }

    /**
     * retrieves all financial allocations saved w/ authenticated user's paycheck
     * @param userId Long
     * @return List of financial responses
     */
    @Transactional(readOnly = true)
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
     * @param financialId Long the ID of the financial item we're looking for
     * @param userId Long the authenticated user's ID
     * @return response with financial information
     */
    @Transactional(readOnly = true)
    public FinancialsResponse findFinancialByID(Long financialId, Long userId) {
        return FinancialsResponse.from(findOwned(financialId, userId));
    }

    /**
     * Deletes a specific financial allocation
     * @param financialId Long
     * @param userId Long
     */
    public void deleteFinancial(Long financialId, Long userId) {
        Financials financial = findOwned(financialId, userId);

        financialsRepository.delete(financial);
    }

    /**
     * Finds a specific financial allocation saved with the authenticated user's paycheck and financial ID and then returns the expenses
     * @param financialId Long the ID of the financial item we're looking for
     * @param userId Long the authenticated user's ID
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

    /**
     * Finds a specific financial allocation saved with the authenticated user's paycheck and financial ID and then returns a single expense from ID
     * @param financialId Long the ID of the financial item we're looking for
     * @param expenseId Long the ID of the expense item we're looking for
     * @param userId Long the authenticated user's ID
     * @return
     */
    @Transactional(readOnly = true)
    public ExpenseResponse findExpenseByID(Long financialId, Long expenseId, Long userId) {
        Financials financial = findOwned(financialId, userId);
        Expense expense = expenseRepository
                .findByIdAndFinancialId(expenseId, financial.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));
        return ExpenseResponse.from(expense);
    }

    /**
     * Deletes a specific expense
     * @param financialId Long
     * @param expenseId Long
     * @param userId Long
     */
    public void deleteExpense(Long financialId, Long expenseId, Long userId){
        Financials financial = findOwned(financialId, userId);
        Expense expense = expenseRepository
                .findByIdAndFinancialId(expenseId, financial.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));

        expenseRepository.delete(expense);
    }

    /**
     * saves or updates an individual category's budget allocation
     * @param paycheck Paycheck object
     * @param request Financials request object
     * @return saved financial entity object
     */
    @Transactional
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

    /**
     * Checks if the user owns the financial record we're looking for
     * @param financialId Long
     * @param userId Long
     * @return financials repository object or a NOT FOUND
     */
    private Financials findOwned(Long financialId, Long userId){
        Paycheck paycheck = paycheckService.findPaycheck(userId);

        return financialsRepository
                .findByIdAndPaycheckId(financialId, paycheck.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));
    }
}