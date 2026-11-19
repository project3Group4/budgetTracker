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
        // pass github's verified identity into the shared provider sign-in logic.
        return signInWithProvider(
                IdentityProvider.GITHUB,
                githubUser.providerUserId(),
                githubUser.email(),
                githubUser.username()
        );
    }

    @Transactional
    public User signInWithProvider(
            IdentityProvider provider,
            String providerUserId,
            String email,
            String displayName
    ) {
        // check whether this provider identity already belongs to an existing user.
        Identity existingIdentity =
                identityRepository
                        .findByProviderAndProviderUserId(
                                provider,
                                providerUserId
                        )
                        .orElse(null);

        // return the existing user when this provider identity has already signed in.
        if (existingIdentity != null) {
            return existingIdentity.getUser();
        }

        // check whether the verified email already belongs to another account.
        User existingUser =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElse(null);

        // reject the login instead of silently creating a duplicate account.
        if (existingUser != null) {
            // find the provider connected to the existing account for the error message.
            String existingProvider =
                    identityRepository
                            .findFirstByUser_Id(existingUser.getId())
                            .map(identity -> identity.getProvider().name())
                            .orElse("another provider");

            // tell the user which provider already owns this email address.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists through "
                            + existingProvider + "."
            );
        }

        // create a new application user from the verified provider information.
        User newUser = new User(
                email,
                displayName,
                Role.USER
        );

        // save the user before creating the identity that references the user.
        User savedUser = userRepository.save(newUser);

        // connect the provider identity to the newly created application user.
        Identity newIdentity = new Identity(
                savedUser,
                provider,
                providerUserId
        );

        // save the provider connection so future logins find the same user.
        identityRepository.save(newIdentity);

        // return the user so the controller can issue the application jwt.
        return savedUser;
    }
}