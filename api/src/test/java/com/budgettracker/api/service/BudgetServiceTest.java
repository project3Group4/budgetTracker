package com.budgettracker.api.service;

import com.budgettracker.api.dto.BudgetRequest;
import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.dto.PaycheckRequest;
import com.budgettracker.api.entity.Paycheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private PaycheckService paycheckService;

    @Mock
    private FinancialsService financialsService;

    @InjectMocks
    private BudgetService budgetService;

    // Tests saving a paycheck and multiple financial allocations
    @Test
    void saveBudgetSuccessfully() {

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        FinancialsRequest groceries = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest bills = new FinancialsRequest(
                2L,
                new BigDecimal("1000.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(groceries, bills)
        );

        Paycheck paycheck = mock(Paycheck.class);

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        budgetService.saveBudget(1L, request);

        verify(paycheckService).savePaycheck(1L, paycheckRequest);
        verify(financialsService).saveFinancial(paycheck, groceries);
        verify(financialsService).saveFinancial(paycheck, bills);
    }

    // Tests saving a paycheck without financial allocations
    @Test
    void saveBudgetWithEmptyFinancials() {

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of()
        );

        Paycheck paycheck = mock(Paycheck.class);

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        budgetService.saveBudget(1L, request);

        verify(paycheckService).savePaycheck(1L, paycheckRequest);
        verifyNoInteractions(financialsService);
    }

    // Tests rejecting duplicate categories
    @Test
    void saveBudgetRejectsDuplicateCategories() {

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        FinancialsRequest first = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest second = new FinancialsRequest(
                1L,
                new BigDecimal("600.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(first, second)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(paycheckService, financialsService);
    }

    // Tests rejecting null financial allocations
    @Test
    void saveBudgetRejectsNullFinancial() {

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                Arrays.asList((FinancialsRequest) null)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(paycheckService, financialsService);
    }

    // Tests saving a budget for a nonexistent user
    @Test
    void saveBudgetThrowsWhenUserNotFound() {

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of()
        );

        when(paycheckService.savePaycheck(99L, paycheckRequest))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(99L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(financialsService);
    }

    // Tests saving a budget with a nonexistent category
    @Test
    void saveBudgetThrowsWhenCategoryNotFound() {

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        FinancialsRequest financialRequest = new FinancialsRequest(
                99L,
                new BigDecimal("500.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(financialRequest)
        );

        Paycheck paycheck = mock(Paycheck.class);

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        doThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Category not found"
        )).when(financialsService)
                .saveFinancial(paycheck, financialRequest);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // NEW: Tests allocations below the available paycheck amount
    @Test
    void saveBudgetAllowsAllocationsBelowAvailableAmount() {

        // $3000 paycheck - $600 savings = $2400 available
        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        FinancialsRequest groceries = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest bills = new FinancialsRequest(
                2L,
                new BigDecimal("1500.00")
        );

        // Total allocations = $2000
        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(groceries, bills)
        );

        Paycheck paycheck = mock(Paycheck.class);

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        assertDoesNotThrow(
                () -> budgetService.saveBudget(1L, request)
        );

        verify(paycheckService).savePaycheck(1L, paycheckRequest);
        verify(financialsService).saveFinancial(paycheck, groceries);
        verify(financialsService).saveFinancial(paycheck, bills);
    }

    // NEW: Tests allocations equal to the available paycheck amount
    @Test
    void saveBudgetAllowsAllocationsEqualToAvailableAmount() {

        // $3000 paycheck - $600 savings = $2400 available
        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        FinancialsRequest groceries = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest bills = new FinancialsRequest(
                2L,
                new BigDecimal("1700.00")
        );

        FinancialsRequest gas = new FinancialsRequest(
                3L,
                new BigDecimal("200.00")
        );

        // Total allocations = $2400
        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(groceries, bills, gas)
        );

        Paycheck paycheck = mock(Paycheck.class);

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        assertDoesNotThrow(
                () -> budgetService.saveBudget(1L, request)
        );

        verify(paycheckService).savePaycheck(1L, paycheckRequest);
        verify(financialsService).saveFinancial(paycheck, groceries);
        verify(financialsService).saveFinancial(paycheck, bills);
        verify(financialsService).saveFinancial(paycheck, gas);
    }

    // NEW: Tests allocations exceeding the available paycheck amount
    @Test
    void saveBudgetRejectsAllocationsExceedingAvailableAmount() {

        // $3000 paycheck - $600 savings = $2400 available
        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        FinancialsRequest groceries = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest bills = new FinancialsRequest(
                2L,
                new BigDecimal("1800.00")
        );

        FinancialsRequest gas = new FinancialsRequest(
                3L,
                new BigDecimal("200.00")
        );

        // Total allocations = $2500, exceeding available funds
        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(groceries, bills, gas)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Verify that invalid budgets are not saved
        verifyNoInteractions(paycheckService, financialsService);
    }
}