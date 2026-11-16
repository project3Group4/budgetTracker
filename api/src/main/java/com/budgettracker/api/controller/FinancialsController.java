package com.budgettracker.api.controller;

import com.budgettracker.api.dto.ExpenseResponse;
import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.service.FinancialsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller for the financials API routes
 */
@RestController
@RequestMapping("/api/v1/user/me/paycheck/financials")
public class FinancialsController {
    private final FinancialsService financialsService;

    /**
     * Constructor for controller
     * @param financialsService the service class
     */
    public FinancialsController(FinancialsService financialsService) {
        this.financialsService = financialsService;
    }

    /**
     * Gets the financials for authenticated user for their current paycheck
     * @param jwt user authentication principal
     * @return List of financial responses
     */
    @GetMapping
    public List<FinancialsResponse> getFinancials(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.getFinancials(userId);
    }

    /**
     * Gets a single financial information for authenticated user for their current paycheck
     * @param financialId the ID of the financial record
     * @param jwt user authentication principal
     * @return A single financial response
     */
    @GetMapping("/{financialId}")
    public FinancialsResponse getFinancialByID(@PathVariable Long financialId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.findFinancialByID(financialId, userId);
    }

    /**
     * Deletes a single financial information for authenticated user for current paycheck
     * @param financialId the ID of the financial record
     * @param jwt user authentication principal
     * @return No content response entity
     */
    @DeleteMapping("/{financialId}")
    public ResponseEntity<Void> deleteFinancial(@PathVariable Long financialId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        financialsService.deleteFinancial(financialId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Gets a list of expenses from a single financial information for authenticated user for their current paycheck
     * @param financialId the ID of the financial record
     * @param jwt user authentication principal
     * @return List of expense responses
     */
    @GetMapping("/{financialId}/expenses")
    public List<ExpenseResponse> getExpensesFromFinancialID(@PathVariable Long financialId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.findFinancialByIDReturnExpenses(financialId, userId);
    }

    /**
     * Gets a single expense from a single financial information for authenticated user for their current paycheck
     * @param financialId the ID of the financial record
     * @param expenseId the ID of the expense record
     * @param jwt user authentication principal
     * @return A single expense response
     */
    @GetMapping("/{financialId}/expenses/{expenseId}")
    public ExpenseResponse getExpenseByID(@PathVariable Long financialId, @PathVariable Long expenseId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.findExpenseByID(financialId, expenseId, userId);
    }

    /**
     * Deletes a single expense from a single financial information for authenticated user for current paycheck
     * @param financialId the ID of the financial record
     * @param expenseId the ID of the expense record
     * @param jwt user authentication principal
     * @return No content response entity
     */
    @DeleteMapping("/{financialId}/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long financialId, @PathVariable Long expenseId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        financialsService.deleteExpense(financialId, expenseId, userId);

        return ResponseEntity.noContent().build();
    }

}