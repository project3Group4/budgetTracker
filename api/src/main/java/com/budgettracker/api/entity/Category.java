package com.budgettracker.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
/**
 * Represents a spending category stored in the database.
 */
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Required by JPA.
     */
    protected Category() {
    }

    /**
     * Creates an active category.
     *
     * @param createdByUser user who created the category
     * @param name category name
     */
    public Category(User createdByUser, String name) {
        this.createdByUser = createdByUser;
        this.name = name;
    }

    /**
     * Creates a category with a specified active status.
     *
     * @param createdByUser user who created the category
     * @param name category name
     * @param isActive whether the category is active
     */
    public Category(User createdByUser, String name, boolean isActive) {
        this.createdByUser = createdByUser;
        this.name = name;
        this.isActive = isActive;
    }

    /**
     * Updates the category's active status.
     *
     * @param active the new active status
     */
    public void updateActive(boolean active) {
        this.isActive = active;
    }
}