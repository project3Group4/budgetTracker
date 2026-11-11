package com.budgettracker.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Table(name = "financials")
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

    protected Financials(){
    }

    public Financials(Paycheck paycheck, Category category, BigDecimal setAmount){
        this.paycheck = paycheck;
        this.category = category;
        this.setAmount = setAmount;
    }
}