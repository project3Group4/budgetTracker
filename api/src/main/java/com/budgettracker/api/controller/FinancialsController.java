package com.budgettracker.api.controller;

import com.budgettracker.api.dto.ExpenseResponse;
import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.service.FinancialsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/me/paycheck/financials")
public class FinancialsController {
    private final FinancialsService financialsService;
    public FinancialsController(FinancialsService financialsService) {
        this.financialsService = financialsService;
    }

    @GetMapping
    public List<FinancialsResponse> getFinancials(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.getFinancials(userId);
    }


    //TODO: TESTS FOR ALL BELOW METHODS

    @GetMapping("/{financialId}")
    public FinancialsResponse getFinancialByID(@PathVariable Long financialId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.findFinancialByID(financialId, userId);
    }

    @DeleteMapping("/{financialId}")
    public ResponseEntity<Void> deleteFinancial(@PathVariable Long financialId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        financialsService.deleteFinancial(financialId, userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{financialId}/expenses")
    public List<ExpenseResponse> getExpensesFromFinancialID(@PathVariable Long financialId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.findFinancialByIDReturnExpenses(financialId, userId);
    }

    @GetMapping("/{financialId}/expenses/{expenseId}")
    public ExpenseResponse getExpenseByID(@PathVariable Long financialId, @PathVariable Long expenseId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.findExpenseByID(financialId, expenseId, userId);
    }

    @DeleteMapping("/{financialId}/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long financialId, @PathVariable Long expenseId, @AuthenticationPrincipal Jwt jwt){
        Long userId = Long.valueOf(jwt.getSubject());

        financialsService.deleteExpense(financialId, expenseId, userId);

        return ResponseEntity.noContent().build();
    }

}