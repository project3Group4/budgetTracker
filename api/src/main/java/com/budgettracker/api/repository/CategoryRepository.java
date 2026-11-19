package com.budgettracker.api.repository;

import com.budgettracker.api.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access for categories.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds only active categories.
     *
     * @param pageable pagination and sorting information
     * @return page of active categories
     */
    Page<Category> findByIsActiveTrue(Pageable pageable);
}