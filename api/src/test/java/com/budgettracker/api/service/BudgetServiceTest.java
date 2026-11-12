package com.budgettracker.api.service;

import com.budgettracker.api.dto.BudgetRequest;
import com.budgettracker.api.dto.PaycheckRequest;
import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.entity.Paycheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;

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

    @Test
    void saveBudgetSavesPaycheckAndFinancials() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        FinancialsRequest financial1 = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest financial2 = new FinancialsRequest(
                2L,
                new BigDecimal("1000.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(financial1, financial2)
        );

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        // Act
        budgetService.saveBudget(1L, request);

        // Assert
        verify(paycheckService)
                .savePaycheck(1L, paycheckRequest);

        verify(financialsService)
                .saveFinancial(paycheck, financial1);

        verify(financialsService)
                .saveFinancial(paycheck, financial2);
    }

    @Test
    void saveBudgetHandlesEmptyFinancials() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of()
        );

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        // Act
        budgetService.saveBudget(1L, request);

        // Assert
        verify(paycheckService)
                .savePaycheck(1L, paycheckRequest);

        verifyNoInteractions(financialsService);
    }

    @Test
    void saveBudgetRejectsDuplicateCategories() {

        // Arrange
        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        FinancialsRequest financial1 = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        FinancialsRequest financial2 = new FinancialsRequest(
                1L,
                new BigDecimal("750.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(financial1, financial2)
        );

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        // Assert
        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Duplicate category",
                exception.getReason()
        );

        verifyNoInteractions(paycheckService);
        verifyNoInteractions(financialsService);
    }

    @Test
    void saveBudgetRejectsNullFinancialAllocation() {

        // Arrange
        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        FinancialsRequest financial = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                Arrays.asList(financial, null)
        );

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        // Assert
        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Financial allocation cannot be null",
                exception.getReason()
        );

        verifyNoInteractions(paycheckService);
        verifyNoInteractions(financialsService);
    }

    @Test
    void saveBudgetRejectsMissingUser() {

        // Arrange
        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        FinancialsRequest financial = new FinancialsRequest(
                1L,
                new BigDecimal("500.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(financial)
        );

        when(paycheckService.savePaycheck(99L, paycheckRequest))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(99L, request)
        );

        // Assert
        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "User not found",
                exception.getReason()
        );

        verify(paycheckService)
                .savePaycheck(99L, paycheckRequest);

        verifyNoInteractions(financialsService);
    }

    @Test
    void saveBudgetRejectsInvalidCategory() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        PaycheckRequest paycheckRequest = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        FinancialsRequest financial = new FinancialsRequest(
                99L,
                new BigDecimal("500.00")
        );

        BudgetRequest request = new BudgetRequest(
                paycheckRequest,
                List.of(financial)
        );

        when(paycheckService.savePaycheck(1L, paycheckRequest))
                .thenReturn(paycheck);

        when(financialsService.saveFinancial(paycheck, financial))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.saveBudget(1L, request)
        );

        // Assert
        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Category not found",
                exception.getReason()
        );

        verify(paycheckService)
                .savePaycheck(1L, paycheckRequest);

        verify(financialsService)
                .saveFinancial(paycheck, financial);
    }
}