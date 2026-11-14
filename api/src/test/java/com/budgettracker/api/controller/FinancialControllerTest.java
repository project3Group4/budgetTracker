package com.budgettracker.api.controller;

import com.budgettracker.api.dto.ExpenseResponse;
import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.security.SecurityConfig;
import com.budgettracker.api.service.FinancialsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(FinancialsController.class)
@Import(SecurityConfig.class)
public class FinancialControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialsService financialsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor asUser() {
        return jwt().jwt(j -> j.subject("5"));
    }

    @Test
    void getFinancialsReturnsIntendedResult() throws Exception{
       FinancialsResponse mockFinancial = new FinancialsResponse(1L, 2L, 3L, new BigDecimal("1200.00"));
       when(financialsService.getFinancials(5L))
               .thenReturn(List.of(mockFinancial));

       mockMvc.perform(get("/api/v1/user/me/paycheck/financials").with(asUser()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getFinancialsWithoutAuthNotAllowed() throws Exception{
        mockMvc.perform(get("/api/v1/user/me/paycheck/financials"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(financialsService);
    }

    @Test
    void getFinancialById() throws Exception{
        FinancialsResponse mockFinancial = new FinancialsResponse(1L, 2L, 3L, new BigDecimal("1200.00"));
        when(financialsService.findFinancialByID(1L, 5L))
                .thenReturn(mockFinancial);

        mockMvc.perform(get("/api/v1/user/me/paycheck/financials/1").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getFinancialByIdWhenNoneExists() throws Exception{
        when(financialsService.findFinancialByID(1L, 5L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));

        mockMvc.perform(get("/api/v1/user/me/paycheck/financials/1").with(asUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFinancialById() throws Exception{
        mockMvc.perform(delete("/api/v1/user/me/paycheck/financials/1").with(asUser()).with(csrf()))
                .andExpect(status().isNoContent());

        verify(financialsService).deleteFinancial(1L, 5L);
    }

    @Test
    void deleteFinancialByIdWhenNoneExists() throws Exception{
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"))
                .when(financialsService).deleteFinancial(1L, 5L);

        mockMvc.perform(delete("/api/v1/user/me/paycheck/financials/1").with(asUser()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpensesReturnsIntendedResult() throws Exception {
        when(financialsService.findFinancialByIDReturnExpenses(1L, 5L)).thenReturn(List.of(
                new ExpenseResponse(11L, 1L, new BigDecimal("500.00"), "Groceries"),
                new ExpenseResponse(12L, 1L, new BigDecimal("200.00"), "Gas")));

        mockMvc.perform(get("/api/v1/user/me/paycheck/financials/1/expenses").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Groceries"))
                .andExpect(jsonPath("$[1].description").value("Gas"));
    }

    @Test
    void getExpensesWhenNoFinancialsExists() throws Exception {
        when(financialsService.findFinancialByIDReturnExpenses(1L, 5L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));

        mockMvc.perform(get("/api/v1/user/me/paycheck/financials/1/expenses").with(asUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpenseByID() throws Exception {
        when(financialsService.findExpenseByID(1L, 11L, 5L))
                .thenReturn(new ExpenseResponse(11L, 1L, new BigDecimal("500.00"), "Groceries"));

        mockMvc.perform(get("/api/v1/user/me/paycheck/financials/1/expenses/11").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.description").value("Groceries"));
    }

    @Test
    void getExpenseByIdWhenNoneExists() throws Exception{
        when(financialsService.findExpenseByID(1L, 11L, 5L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"));

        mockMvc.perform(get("/api/v1/user/me/paycheck/financials/1/expenses/11").with(asUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpenseById() throws Exception{
        mockMvc.perform(delete("/api/v1/user/me/paycheck/financials/1/expenses/11").with(asUser()).with(csrf()))
                .andExpect(status().isNoContent());

        verify(financialsService).deleteExpense(1L, 11L, 5L);
    }

    @Test
    void deleteExpenseByIdWhenNoneExists() throws Exception{
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Financial record not found"))
                .when(financialsService).deleteExpense(1L, 11L, 5L);

        mockMvc.perform(delete("/api/v1/user/me/paycheck/financials/1/expenses/11").with(asUser()).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
