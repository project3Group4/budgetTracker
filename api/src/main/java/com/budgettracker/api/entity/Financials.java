package com.budgettracker.api.entity;

import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * Financials database table
 */
@Entity
@Table(name = "financials", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"paycheck_id", "category_id"})

})
@Getter
public class Financials{
    //primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financial_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paycheck_id", nullable = false)
    private Paycheck paycheck;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    //amount set by user for that category
    @Column(name = "set_amount", nullable = false)
    private BigDecimal setAmount;

    /**
     * Default constructor for Financials table
     */
    protected Financials(){
    }

    /**
     * Parameterized constructor for Financials table
     * @param paycheck Paycheck object
     * @param category Category object
     * @param setAmount BigDecimal
     */
    public Financials(Paycheck paycheck, Category category, BigDecimal setAmount){
        this.paycheck = paycheck;
        this.category = category;
        this.setAmount = setAmount;
    }

    /**
     * allows the mainpage to update the financials info in the database whenever user enters new paycheck
     * @param setAmount BigDecimal
     */
    public void updateSetAmount(BigDecimal setAmount){
        this.setAmount = setAmount;
    }
}