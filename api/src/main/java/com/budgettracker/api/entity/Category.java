package com.budgettracker.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
public class Category{

    @Id
    //creates unique ID for each category
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    //category name
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    protected Category(){
    }

    public Category(User createdByUser, String name){
        this.createdByUser = createdByUser;
        this.name = name;
    }
}