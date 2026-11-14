package com.budgettracker.api.controller;

import com.budgettracker.api.dto.PaycheckRequest;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.security.SecurityConfig;
import com.budgettracker.api.service.PaycheckService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(PaycheckController.class)
@Import(SecurityConfig.class)
class PaycheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaycheckService paycheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createsNewPaycheck() throws Exception {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        when(paycheck.getId()).thenReturn(1L);
        when(paycheck.getPaycheckAmount())
                .thenReturn(new BigDecimal("2500.00"));
        when(paycheck.getSavingsPercent())
                .thenReturn(new BigDecimal("10.00"));

        when(paycheckService.savePaycheckWithResult(
                eq(1L), any(PaycheckRequest.class)
        )).thenReturn(
                new PaycheckService.PaycheckSaveResult(
                        paycheck, true
                )
        );

        // Act and Assert
        mockMvc.perform(
                        put("/api/v1/user/me/paycheck")
                                .with(jwt().jwt(token -> token.subject("1")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "paycheckAmount": 2500.00,
                                    "savingsPercent": 10.00
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paycheckAmount").value(2500.00))
                .andExpect(jsonPath("$.savingsPercent").value(10.00));
    }

    @Test
    void updatesExistingPaycheck() throws Exception {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        when(paycheck.getId()).thenReturn(1L);
        when(paycheck.getPaycheckAmount())
                .thenReturn(new BigDecimal("3000.00"));
        when(paycheck.getSavingsPercent())
                .thenReturn(new BigDecimal("20.00"));

        when(paycheckService.savePaycheckWithResult(
                eq(1L), any(PaycheckRequest.class)
        )).thenReturn(
                new PaycheckService.PaycheckSaveResult(
                        paycheck, false
                )
        );

        // Act and Assert
        mockMvc.perform(
                        put("/api/v1/user/me/paycheck")
                                .with(jwt().jwt(token -> token.subject("1")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "paycheckAmount": 3000.00,
                                    "savingsPercent": 20.00
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paycheckAmount").value(3000.00))
                .andExpect(jsonPath("$.savingsPercent").value(20.00));
    }

    @Test
    void deleteExistingPaycheckWithCorrectUser() throws Exception {
        mockMvc.perform(delete("/api/v1/user/me/paycheck")
                        .with(jwt().jwt(j -> j.subject("5")))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(paycheckService).deletePaycheck(5L);
    }

    @Test
    void deletingNotExistingPaycheckWhenNoneExists() throws Exception{
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paycheck not found"))
                .when(paycheckService).deletePaycheck(5L);

        mockMvc.perform(delete("/api/v1/user/me/paycheck")
                        .with(jwt().jwt(j -> j.subject("5")))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingExistingPaycheckWithoutCorrectUserIsUnauthorized() throws Exception{
        mockMvc.perform(delete("/api/v1/user/me/paycheck").with(csrf()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(paycheckService);
    }
}