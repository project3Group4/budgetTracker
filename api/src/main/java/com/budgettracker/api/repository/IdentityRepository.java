package com.budgettracker.api.repository;

import com.budgettracker.api.entity.Identity;
import com.budgettracker.api.entity.IdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdentityRepository extends JpaRepository<Identity, Long> {

    Optional<Identity> findByProviderAndProviderUserId(
            IdentityProvider provider,
            String providerUserId
    );

    Optional<Identity> findFirstByUser_Id(Long userId);
}