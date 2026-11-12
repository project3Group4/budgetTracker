package com.budgettracker.api.auth;

import com.budgettracker.api.entity.Identity;
import com.budgettracker.api.entity.IdentityProvider;
import com.budgettracker.api.entity.Role;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.IdentityRepository;
import com.budgettracker.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityRepository identityRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void createsUserAndGitHubIdentityForNewAccount() {
        // create fake github account information for the test
        GitHubIdentityClient.VerifiedGitHubUser githubUser =
                new GitHubIdentityClient.VerifiedGitHubUser(
                        "42",
                        "octocat",
                        "octocat@example.com"
                );

        // create the user that the mocked repository will return after saving
        User savedUser = new User(
                githubUser.email(),
                githubUser.username(),
                Role.USER
        );

        // tell the mocked repositories that this is a new account
        when(identityRepository.findByProviderAndProviderUserId(
                IdentityProvider.GITHUB,
                githubUser.providerUserId()
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase(githubUser.email()))
                .thenReturn(Optional.empty());

        // return the fake saved user when the service saves the user
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // run the github sign-in logic
        User result = authService.signInWithGitHub(githubUser);

        // confirm that the saved user is returned
        assertSame(savedUser, result);

        // capture and inspect the user sent to the repository
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertEquals(
                githubUser.email(),
                userCaptor.getValue().getEmail()
        );
        assertEquals(
                githubUser.username(),
                userCaptor.getValue().getDisplayName()
        );
        assertEquals(
                Role.USER,
                userCaptor.getValue().getRole()
        );

        // capture and inspect the github identity sent to the repository
        ArgumentCaptor<Identity> identityCaptor =
                ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(identityCaptor.capture());

        assertSame(
                savedUser,
                identityCaptor.getValue().getUser()
        );
        assertEquals(
                IdentityProvider.GITHUB,
                identityCaptor.getValue().getProvider()
        );
        assertEquals(
                githubUser.providerUserId(),
                identityCaptor.getValue().getProviderUserId()
        );
    }

    @Test
    void existingGitHubIdentityDoesNotCreateDuplicates() {
        // create fake github account information for the test
        GitHubIdentityClient.VerifiedGitHubUser githubUser =
                new GitHubIdentityClient.VerifiedGitHubUser(
                        "42",
                        "octocat",
                        "octocat@example.com"
                );

        // create an existing user and github identity
        User existingUser = new User(
                githubUser.email(),
                githubUser.username(),
                Role.USER
        );

        Identity existingIdentity = new Identity(
                existingUser,
                IdentityProvider.GITHUB,
                githubUser.providerUserId()
        );

        // tell the repository that this github identity already exists
        when(identityRepository.findByProviderAndProviderUserId(
                IdentityProvider.GITHUB,
                githubUser.providerUserId()
        )).thenReturn(Optional.of(existingIdentity));

        // run the github sign-in logic
        User result = authService.signInWithGitHub(githubUser);

        // confirm that the existing user is returned
        assertSame(existingUser, result);

        // confirm that no duplicate user or identity was created
        verify(userRepository, never()).save(any(User.class));
        verify(identityRepository, never()).save(any(Identity.class));
    }

    @Test
    void duplicateEmailIsRejected() {
        // create fake github account information for the test
        GitHubIdentityClient.VerifiedGitHubUser githubUser =
                new GitHubIdentityClient.VerifiedGitHubUser(
                        "42",
                        "octocat",
                        "octocat@example.com"
                );

        // create an existing user with the same email
        User existingUser = new User(
                githubUser.email(),
                "existing-user",
                Role.USER
        );

        // create an existing identity connected to that user
        Identity existingIdentity = new Identity(
                existingUser,
                IdentityProvider.GITHUB,
                "different-provider-id"
        );

        // tell the repositories that the github id is new
        when(identityRepository.findByProviderAndProviderUserId(
                IdentityProvider.GITHUB,
                githubUser.providerUserId()
        )).thenReturn(Optional.empty());

        // tell the user repository that the email is already registered
        when(userRepository.findByEmailIgnoreCase(githubUser.email()))
                .thenReturn(Optional.of(existingUser));

        when(identityRepository.findFirstByUser_Id(existingUser.getId()))
                .thenReturn(Optional.of(existingIdentity));

        // confirm that the service rejects the duplicate email
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.signInWithGitHub(githubUser)
        );

        // confirm that the service returns a conflict response
        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        // confirm that no new records were created
        verify(userRepository, never()).save(any(User.class));
        verify(identityRepository, never()).save(any(Identity.class));
    }
}