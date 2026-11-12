package com.budgettracker.api.repository;

import com.budgettracker.api.entity.Paycheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//tells Spring Data JPA that the repo manages Paycheck entities, and its PK is Long values
public interface PaycheckRepository extends JpaRepository<Paycheck, Long>{
    Optional<Paycheck> findByUserId(Long userId);
}