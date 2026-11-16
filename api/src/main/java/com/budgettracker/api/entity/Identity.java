package com.budgettracker.api.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Identities database table
 */
@Entity
@Table(
        name = "identities",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_identity_provider_user",
                columnNames = {"provider", "provider_user_id"}
        )
)
public class Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private IdentityProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Default constructor for Identity table
     */
    protected Identity() {
    }

    /**
     * Parameterized constructor for Identity table
     * @param user User object
     * @param provider IdentityProvider (GITHUB or GOOGLE)
     * @param providerUserId String
     */
    public Identity(User user, IdentityProvider provider, String providerUserId) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public IdentityProvider getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}