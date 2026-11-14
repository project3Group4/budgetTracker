package com.budgettracker.api.repository;

import com.budgettracker.api.entity.Financials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for financials
 */
public interface FinancialsRepository extends JpaRepository<Financials, Long> {
    List<Financials> findByPaycheckId(Long paycheckId);

    Optional<Financials> findByIdAndPaycheckId(Long id, Long paycheckId);

    Optional<Financials> findByPaycheckIdAndCategoryId(Long paycheckId, Long categoryId);
}