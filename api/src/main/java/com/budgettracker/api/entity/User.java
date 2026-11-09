package com.budgettracker.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

/**
 * User database table
 * lombok automatically creates getters with @Getter
 */
@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; //enum because the only roles we have should be USER or ADMIN

    /**
     * Default constructor for User table
     */
    protected User() {
    }
    /**
     * Parameterized constructor for User table
     * @param email user's email
     * @param displayName user's display_name
     * @param role user's role (USER or ADMIN)
     */
    public User(String email, String displayName, Role role) {
        this.email = email;
        this.displayName = displayName;
        this.role = (role != null) ? role : Role.USER;
    }

    /*
    This changes a role, will need to be uncommented when the API is created
    public void changeRole(Role role){
        this.role = role;
    }
     */
}