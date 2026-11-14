package com.budgettracker.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * Paycheck database table
 */
@Entity
@Table(name = "paycheck")
@Getter
public class Paycheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paycheck_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "paycheck_amount", nullable = false)
    private BigDecimal paycheckAmount;

    @Column(name = "savings_percent", nullable = false)
    private BigDecimal savingsPercent;

    /**
     * Default constructor for Paycheck table
     */
    protected Paycheck(){
    }

    /**
     * Parameterized constructor for Paycheck table
     * @param user User object
     * @param paycheckAmount BigDecimal
     * @param savingsPercent BigDecimal
     */
    public Paycheck(User user, BigDecimal paycheckAmount, BigDecimal savingsPercent){
        this.user = user;
        this.paycheckAmount = paycheckAmount;
        this.savingsPercent = savingsPercent;
    }

    /**
     * allows the mainpage to update the paycheck info in the database whenever user enters new paycheck
     * @param paycheckAmount BigDecimal
     * @param savingsPercent BigDecimal
     */
    public void updatePaycheck(BigDecimal paycheckAmount,
                               BigDecimal savingsPercent) {
        this.paycheckAmount = paycheckAmount;
        this.savingsPercent = savingsPercent;
    }
}