
package com.budgettracker.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Table(name = "expenses")
@Getter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_id", nullable = false)
    private Financials financial;

    @Column(name = "amount", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    protected Expense() {
    }

    public Expense(Financials financial,
                   BigDecimal amount,
                   String description) {
        this.financial = financial;
        this.amount = amount;
        this.description = description;
    }
}
