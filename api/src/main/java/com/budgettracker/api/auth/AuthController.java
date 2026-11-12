package com.budgettracker.api.auth;

import com.budgettracker.api.entity.User;
import com.budgettracker.api.security.AppTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GitHubIdentityClient gitHubIdentityClient;
    private final AuthService authService;
    private final AppTokenService appTokenService;

    public AuthController(
            GitHubIdentityClient gitHubIdentityClient,
            AuthService authService,
            AppTokenService appTokenService
    ) {
        this.gitHubIdentityClient = gitHubIdentityClient;
        this.authService = authService;
        this.appTokenService = appTokenService;
    }

    @PostMapping("/github")
    public AuthResponse signInWithGitHub(
            @Valid @RequestBody GitHubAuthRequest request
    ) {
        GitHubIdentityClient.VerifiedGitHubUser githubUser =
                gitHubIdentityClient.exchangeCodeForIdentity(request);

        User user = authService.signInWithGitHub(githubUser);
        String token = appTokenService.issueFor(user);

        return new AuthResponse(
                token,
                new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRole().name()
                )
        );
    }

    public record AuthResponse(
            String token,
            UserResponse user
    ) {
    }

    public record UserResponse(
            Long id,
            String email,
            String displayName,
            String role
    ) {
    }
}