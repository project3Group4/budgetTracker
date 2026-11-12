package com.budgettracker.api.service;

import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.Financials;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.repository.FinancialsRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialsServiceTest {

    @Mock
    private FinancialsRepository financialsRepository;

    @Mock
    private PaycheckService paycheckService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private FinancialsService financialsService;

    @Test
    void getFinancialsReturnsExistingAllocations() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);
        Category category = mock(Category.class);
        Financials financial = mock(Financials.class);

        when(paycheck.getId()).thenReturn(1L);
        when(category.getId()).thenReturn(2L);

        when(financial.getId()).thenReturn(3L);
        when(financial.getPaycheck()).thenReturn(paycheck);
        when(financial.getCategory()).thenReturn(category);
        when(financial.getSetAmount())
                .thenReturn(new BigDecimal("500.00"));

        when(paycheckService.findPaycheck(1L))
                .thenReturn(paycheck);

        when(financialsRepository.findByPaycheckId(1L))
                .thenReturn(List.of(financial));

        // Act
        List<FinancialsResponse> result =
                financialsService.getFinancials(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).id());
        assertEquals(1L, result.get(0).paycheckId());
        assertEquals(2L, result.get(0).categoryId());

        assertEquals(
                new BigDecimal("500.00"),
                result.get(0).setAmount()
        );

        verify(paycheckService).findPaycheck(1L);
        verify(financialsRepository).findByPaycheckId(1L);
    }

    @Test
    void getFinancialsReturnsEmptyList() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        when(paycheck.getId()).thenReturn(1L);

        when(paycheckService.findPaycheck(1L))
                .thenReturn(paycheck);

        when(financialsRepository.findByPaycheckId(1L))
                .thenReturn(List.of());

        // Act
        List<FinancialsResponse> result =
                financialsService.getFinancials(1L);

        // Assert
        assertTrue(result.isEmpty());

        verify(paycheckService).findPaycheck(1L);
        verify(financialsRepository).findByPaycheckId(1L);
    }

    @Test
    void saveFinancialCreatesNewAllocation() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);
        Category category = mock(Category.class);

        when(paycheck.getId()).thenReturn(1L);
        when(category.getId()).thenReturn(2L);

        FinancialsRequest request = new FinancialsRequest(
                2L,
                new BigDecimal("500.00")
        );

        when(categoryService.getAvailableCategory(2L))
                .thenReturn(category);

        when(financialsRepository
                .findByPaycheckIdAndCategoryId(1L, 2L))
                .thenReturn(Optional.empty());

        when(financialsRepository.save(any(Financials.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Financials result =
                financialsService.saveFinancial(paycheck, request);

        // Assert
        assertNotNull(result);
        assertSame(paycheck, result.getPaycheck());
        assertSame(category, result.getCategory());

        assertEquals(
                new BigDecimal("500.00"),
                result.getSetAmount()
        );

        verify(categoryService).getAvailableCategory(2L);

        verify(financialsRepository)
                .findByPaycheckIdAndCategoryId(1L, 2L);

        verify(financialsRepository).save(result);
    }

    @Test
    void saveFinancialUpdatesExistingAllocation() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);
        Category category = mock(Category.class);

        when(paycheck.getId()).thenReturn(1L);
        when(category.getId()).thenReturn(2L);

        Financials existingFinancial = new Financials(
                paycheck,
                category,
                new BigDecimal("500.00")
        );

        FinancialsRequest request = new FinancialsRequest(
                2L,
                new BigDecimal("750.00")
        );

        when(categoryService.getAvailableCategory(2L))
                .thenReturn(category);

        when(financialsRepository
                .findByPaycheckIdAndCategoryId(1L, 2L))
                .thenReturn(Optional.of(existingFinancial));

        when(financialsRepository.save(existingFinancial))
                .thenReturn(existingFinancial);

        // Act
        Financials result =
                financialsService.saveFinancial(paycheck, request);

        // Assert
        assertSame(existingFinancial, result);
        assertSame(paycheck, result.getPaycheck());
        assertSame(category, result.getCategory());

        assertEquals(
                new BigDecimal("750.00"),
                result.getSetAmount()
        );

        verify(categoryService).getAvailableCategory(2L);

        verify(financialsRepository)
                .findByPaycheckIdAndCategoryId(1L, 2L);

        verify(financialsRepository).save(existingFinancial);
    }

    @Test
    void getFinancialsRejectsMissingPaycheck() {

        // Arrange
        when(paycheckService.findPaycheck(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paycheck not found"
                ));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> financialsService.getFinancials(1L)
        );

        // Assert
        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Paycheck not found",
                exception.getReason()
        );

        verify(paycheckService).findPaycheck(1L);
        verifyNoInteractions(financialsRepository);
    }

    @Test
    void saveFinancialRejectsMissingCategory() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        FinancialsRequest request = new FinancialsRequest(
                99L,
                new BigDecimal("500.00")
        );

        when(categoryService.getAvailableCategory(99L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> financialsService.saveFinancial(
                        paycheck, request
                )
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

        verify(categoryService).getAvailableCategory(99L);
        verifyNoInteractions(financialsRepository);
    }

    @Test
    void saveFinancialRejectsInactiveCategory() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        FinancialsRequest request = new FinancialsRequest(
                2L,
                new BigDecimal("500.00")
        );

        when(categoryService.getAvailableCategory(2L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category is inactive"
                ));

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> financialsService.saveFinancial(
                        paycheck, request
                )
        );

        // Assert
        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                "Category is inactive",
                exception.getReason()
        );

        verify(categoryService).getAvailableCategory(2L);
        verifyNoInteractions(financialsRepository);
    }
}