package com.budgettracker.api.service;

import com.budgettracker.api.dto.PaycheckRequest;
import com.budgettracker.api.dto.PaycheckResponse;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.entity.Role;
import com.budgettracker.api.repository.PaycheckRepository;
import com.budgettracker.api.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaycheckServiceTest {

    @Mock
    private PaycheckRepository paycheckRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaycheckService paycheckService;

    @Test
    void getPaycheckReturnsExistingPaycheck() {

        // Arrange
        Paycheck paycheck = mock(Paycheck.class);

        when(paycheck.getId()).thenReturn(1L);

        when(paycheck.getPaycheckAmount())
                .thenReturn(new BigDecimal("2500.00"));

        when(paycheck.getSavingsPercent())
                .thenReturn(new BigDecimal("10.00"));

        when(paycheckRepository.findByUserId(1L))
                .thenReturn(Optional.of(paycheck));

        // Act
        PaycheckResponse result = paycheckService.getPaycheck(1L);

        // Assert
        assertEquals(1L, result.id());
        assertEquals(new BigDecimal("2500.00"), result.paycheckAmount());
        assertEquals(new BigDecimal("10.00"), result.savingsPercent());

        verify(paycheckRepository).findByUserId(1L);
    }

    @Test
    void savePaycheckWithResultCreatesNewPaycheck() {

        // Arrange
        User user = new User(
                "test@example.com",
                "Test User",
                Role.USER
        );

        PaycheckRequest request = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(paycheckRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(paycheckRepository.save(any(Paycheck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaycheckService.PaycheckSaveResult result =
                paycheckService.savePaycheckWithResult(1L, request);

        // Assert
        assertTrue(result.created());
        assertSame(user, result.paycheck().getUser());

        assertEquals(
                new BigDecimal("2500.00"),
                result.paycheck().getPaycheckAmount()
        );

        assertEquals(
                new BigDecimal("10.00"),
                result.paycheck().getSavingsPercent()
        );

        verify(userRepository).findById(1L);
        verify(paycheckRepository).findByUserId(1L);
        verify(paycheckRepository).save(any(Paycheck.class));
    }

    @Test
    void savePaycheckWithResultUpdatesExistingPaycheck() {

        // Arrange
        User user = new User(
                "test@example.com",
                "Test User",
                Role.USER
        );

        Paycheck existingPaycheck = new Paycheck(
                user,
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        PaycheckRequest request = new PaycheckRequest(
                new BigDecimal("3000.00"),
                new BigDecimal("20.00")
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(paycheckRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingPaycheck));

        when(paycheckRepository.save(existingPaycheck))
                .thenReturn(existingPaycheck);

        // Act
        PaycheckService.PaycheckSaveResult result =
                paycheckService.savePaycheckWithResult(1L, request);

        // Assert
        assertFalse(result.created());
        assertSame(existingPaycheck, result.paycheck());
        assertSame(user, result.paycheck().getUser());

        assertEquals(
                new BigDecimal("3000.00"),
                result.paycheck().getPaycheckAmount()
        );

        assertEquals(
                new BigDecimal("20.00"),
                result.paycheck().getSavingsPercent()
        );

        verify(userRepository).findById(1L);
        verify(paycheckRepository).findByUserId(1L);
        verify(paycheckRepository).save(existingPaycheck);
    }

    @Test
    void savePaycheckRejectsMissingUser() {

        // Arrange
        PaycheckRequest request = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paycheckService.savePaycheckWithResult(99L, request)
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());

        verify(userRepository).findById(99L);
        verifyNoInteractions(paycheckRepository);
    }

    @Test
    void getPaycheckRejectsMissingPaycheck() {

        // Arrange
        when(paycheckRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paycheckService.getPaycheck(1L)
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Paycheck not found", exception.getReason());

        verify(paycheckRepository).findByUserId(1L);
    }


    @Test
    void savePaycheckReturnsSavedPaycheck() {

        // Arrange
        User user = new User(
                "test@example.com",
                "Test User",
                Role.USER
        );

        PaycheckRequest request = new PaycheckRequest(
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(paycheckRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(paycheckRepository.save(any(Paycheck.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Paycheck result = paycheckService.savePaycheck(1L, request);

        // Assert
        assertNotNull(result);
        assertSame(user, result.getUser());

        assertEquals(
                new BigDecimal("2500.00"),
                result.getPaycheckAmount()
        );

        assertEquals(
                new BigDecimal("10.00"),
                result.getSavingsPercent()
        );

        verify(paycheckRepository).save(result);
    }


}