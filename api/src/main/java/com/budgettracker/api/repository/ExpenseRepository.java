
package com.budgettracker.api.repository;

import com.budgettracker.api.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {
}
