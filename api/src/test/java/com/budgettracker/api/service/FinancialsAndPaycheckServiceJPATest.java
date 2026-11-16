package com.budgettracker.api.service;

import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.Expense;
import com.budgettracker.api.entity.Financials;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.entity.Role;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.ExpenseRepository;
import com.budgettracker.api.repository.FinancialsRepository;
import com.budgettracker.api.repository.PaycheckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This tests to make sure that the cascade all actually does what it's supposed to do
 */
@DataJpaTest
@Import({FinancialsService.class, PaycheckService.class, CategoryService.class})
public class FinancialsAndPaycheckServiceJPATest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaycheckService paycheckService;

    @Autowired
    private FinancialsService financialsService;

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * This checks to make sure deleting one expense doesn't delete the others
     */
    @Test
    void deletingOneExpenseKeepsEverythingElse(){
        User user = entityManager.persist(new User("email@gmail.com", "newUser", Role.USER));
        Category category = entityManager.persist(new Category(user, "Rent"));
        Paycheck paycheck = entityManager.persist(new Paycheck(user, new BigDecimal("700.00"), new BigDecimal("20.00")));
        Financials financial = entityManager.persist(new Financials(paycheck, category, new BigDecimal("1800.00")));
        Expense expense = entityManager.persist(new Expense(financial, new BigDecimal("900.00"), "First half"));
        Expense expense2 = entityManager.persist(new Expense(financial, new BigDecimal("900.00"), "Second half"));

        entityManager.flush();
        entityManager.clear();

        financialsService.deleteExpense(financial.getId(), expense.getId(), user.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Expense.class, expense.getId())).isNull();
        assertThat(entityManager.find(Expense.class, expense2.getId())).isNotNull();
        assertThat(entityManager.find(Financials.class, financial.getId())).isNotNull();
        assertThat(entityManager.find(Paycheck.class, paycheck.getId())).isNotNull();
    }

    /**
     * This checks to make sure deleting the financial record deletes it's expenses too
     */
    @Test
    void deletingFinancialDeletesExpenses(){
        User user = entityManager.persist(new User("email@gmail.com", "newUser", Role.USER));
        Category category = entityManager.persist(new Category(user, "Rent"));
        Paycheck paycheck = entityManager.persist(new Paycheck(user, new BigDecimal("700.00"), new BigDecimal("20.00")));
        Financials financial = entityManager.persist(new Financials(paycheck, category, new BigDecimal("1800.00")));
        Expense expense = entityManager.persist(new Expense(financial, new BigDecimal("900.00"), "First half"));
        Expense expense2 = entityManager.persist(new Expense(financial, new BigDecimal("900.00"), "Second half"));

        entityManager.flush();
        entityManager.clear();

        financialsService.deleteFinancial(financial.getId(), user.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Expense.class, expense.getId())).isNull();
        assertThat(entityManager.find(Expense.class, expense2.getId())).isNull();
        assertThat(entityManager.find(Financials.class, financial.getId())).isNull();
        assertThat(entityManager.find(Paycheck.class, paycheck.getId())).isNotNull();
    }

    /**
     * This checks to make sure deleting the paycheck deletes all of it's children too
     */
    @Test
    void deletingPaycheckCascades(){
        User user = entityManager.persist(new User("email@gmail.com", "newUser", Role.USER));
        Category category = entityManager.persist(new Category(user, "Rent"));
        Paycheck paycheck = entityManager.persist(new Paycheck(user, new BigDecimal("700.00"), new BigDecimal("20.00")));
        Financials financial = entityManager.persist(new Financials(paycheck, category, new BigDecimal("1800.00")));
        Expense expense = entityManager.persist(new Expense(financial, new BigDecimal("900.00"), "First half"));
        Expense expense2 = entityManager.persist(new Expense(financial, new BigDecimal("900.00"), "Second half"));

        entityManager.flush();
        entityManager.clear();

        paycheckService.deletePaycheck(user.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Expense.class, expense.getId())).isNull();
        assertThat(entityManager.find(Expense.class, expense2.getId())).isNull();
        assertThat(entityManager.find(Financials.class, financial.getId())).isNull();
        assertThat(entityManager.find(Paycheck.class, paycheck.getId())).isNull();
    }

}
