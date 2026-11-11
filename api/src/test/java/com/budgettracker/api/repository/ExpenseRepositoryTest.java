
package com.budgettracker.api.repository;

import com.budgettracker.api.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveExpense() {

        // Arrange: Create a user
        User user = new User(
                "test@example.com",
                "Test User",
                Role.USER
        );
        entityManager.persist(user);

        // Create a paycheck
        Paycheck paycheck = new Paycheck(
                user,
                new BigDecimal("1000.00"),
                new BigDecimal("20.00")
        );
        entityManager.persist(paycheck);

        // Create a category
        Category category = new Category(
                null,
                "Groceries"
        );
        entityManager.persist(category);

        // Create financial allocation
        Financials financials = new Financials(
                paycheck,
                category,
                new BigDecimal("200.00")
        );
        entityManager.persist(financials);

        // Create an expense
        Expense expense = new Expense(
                financials,
                new BigDecimal("35.50"),
                "Weekly groceries"
        );

        // Act: Save expense
        Expense saved = expenseRepository.saveAndFlush(expense);

        Long expenseId = saved.getId();

        entityManager.clear();

        Expense retrieved = expenseRepository
                .findById(expenseId)
                .orElseThrow();

        // Assert
        assertNotNull(retrieved.getId());

        assertEquals(
                0,
                new BigDecimal("35.50")
                        .compareTo(retrieved.getAmount())
        );

        assertEquals(
                "Weekly groceries",
                retrieved.getDescription()
        );

        assertEquals(
                financials.getId(),
                retrieved.getFinancial().getId()
        );
    }
}
