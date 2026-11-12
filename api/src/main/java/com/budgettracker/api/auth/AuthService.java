package com.budgettracker.api.auth;

import com.budgettracker.api.entity.Identity;
import com.budgettracker.api.entity.IdentityProvider;
import com.budgettracker.api.entity.Role;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.IdentityRepository;
import com.budgettracker.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final IdentityRepository identityRepository;

    public AuthService(
            UserRepository userRepository,
            IdentityRepository identityRepository
    ) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
    }

    @Transactional
    public User signInWithGitHub(
            GitHubIdentityClient.VerifiedGitHubUser githubUser
    ) {
        Identity existingIdentity =
                identityRepository
                        .findByProviderAndProviderUserId(
                                IdentityProvider.GITHUB,
                                githubUser.providerUserId()
                        )
                        .orElse(null);

        if (existingIdentity != null) {
            return existingIdentity.getUser();
        }

        User existingUser =
                userRepository
                        .findByEmailIgnoreCase(githubUser.email())
                        .orElse(null);

        if (existingUser != null) {
            String existingProvider =
                    identityRepository
                            .findFirstByUser_Id(existingUser.getId())
                            .map(identity -> identity.getProvider().name())
                            .orElse("another provider");

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists through "
                            + existingProvider + "."
            );
        }

        User newUser = new User(
                githubUser.email(),
                githubUser.username(),
                Role.USER
        );

        User savedUser = userRepository.save(newUser);

        Identity newIdentity = new Identity(
                savedUser,
                IdentityProvider.GITHUB,
                githubUser.providerUserId()
        );

        identityRepository.save(newIdentity);

        return savedUser;
    }
}