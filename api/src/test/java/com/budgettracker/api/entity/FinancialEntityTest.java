package com.budgettracker.api.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialEntityTest {

    @Test
    void categoryStoresUserNameAndDefaultsToActive() {
        // create the user and category used by the test
        User user = new User(
                "test@example.com",
                "test-user",
                Role.USER
        );

        Category category = new Category(user, "Rent");

        // confirm that the category stores its values
        assertSame(user, category.getCreatedByUser());
        assertEquals("Rent", category.getName());

        // confirm that new categories are active by default
        assertTrue(category.isActive());
    }

    @Test
    void paycheckStoresUserAmountAndSavingsPercent() {
        // create the user and paycheck used by the test
        User user = new User(
                "test@example.com",
                "test-user",
                Role.USER
        );

        BigDecimal paycheckAmount =
                new BigDecimal("2500.00");

        BigDecimal savingsPercent =
                new BigDecimal("10.00");

        Paycheck paycheck = new Paycheck(
                user,
                paycheckAmount,
                savingsPercent
        );

        // confirm that the paycheck stores all provided values
        assertSame(user, paycheck.getUser());
        assertEquals(paycheckAmount, paycheck.getPaycheckAmount());
        assertEquals(savingsPercent, paycheck.getSavingsPercent());
    }

    @Test
    void financialsStoresPaycheckCategoryAndAmount() {
        // create the objects needed by the financials record
        User user = new User(
                "test@example.com",
                "test-user",
                Role.USER
        );

        Category category = new Category(user, "Rent");

        Paycheck paycheck = new Paycheck(
                user,
                new BigDecimal("2500.00"),
                new BigDecimal("10.00")
        );

        BigDecimal setAmount =
                new BigDecimal("1200.00");

        Financials financials = new Financials(
                paycheck,
                category,
                setAmount
        );

        // confirm that financials stores all provided values
        assertSame(paycheck, financials.getPaycheck());
        assertSame(category, financials.getCategory());
        assertEquals(setAmount, financials.getSetAmount());
    }
}