
package com.budgettracker.api.repository;

import com.budgettracker.api.entity.Expense;
import com.budgettracker.api.entity.Financials;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for expenses
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByFinancialId(Long financialId);

    Optional<Expense> findByIdAndFinancialId(Long id, Long financialId);
}
