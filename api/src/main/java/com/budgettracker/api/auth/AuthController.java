package com.budgettracker.api.auth;

import com.budgettracker.api.entity.IdentityProvider;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.security.AppTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GitHubIdentityClient gitHubIdentityClient;
    private final GoogleIdentityClient googleIdentityClient;
    private final AuthService authService;
    private final AppTokenService appTokenService;

    public AuthController(
            GitHubIdentityClient gitHubIdentityClient,
            GoogleIdentityClient googleIdentityClient,
            AuthService authService,
            AppTokenService appTokenService
    ) {
        // store the provider clients and application services used by this controller.
        this.gitHubIdentityClient = gitHubIdentityClient;
        this.googleIdentityClient = googleIdentityClient;
        this.authService = authService;
        this.appTokenService = appTokenService;
    }

    @PostMapping("/github")
    public AuthResponse signInWithGitHub(
            @Valid @RequestBody GitHubAuthRequest request
    ) {
        // exchange the github code for a verified github identity.
        GitHubIdentityClient.VerifiedGitHubUser githubUser =
                gitHubIdentityClient.exchangeCodeForIdentity(request);

        // find or create the application user connected to github.
        User user = authService.signInWithGitHub(githubUser);

        // create the application's jwt for future api requests.
        String token = appTokenService.issueFor(user);

        // return the jwt and user information to the expo app.
        return createAuthResponse(user, token);
    }

    @PostMapping("/google")
    public AuthResponse signInWithGoogle(
            @RequestBody GoogleAuthRequest request
    ) {
        // verify the google id token or exchange and verify the web authorization code.
        GoogleIdentityClient.VerifiedGoogleUser googleUser =
                googleIdentityClient.verify(request);

        // find or create the application user connected to google.
        User user = authService.signInWithProvider(
                IdentityProvider.GOOGLE,
                googleUser.providerUserId(),
                googleUser.email(),
                googleUser.displayName()
        );

        // create the application's jwt for future api requests.
        String token = appTokenService.issueFor(user);

        // return the jwt and user information to the expo app.
        return createAuthResponse(user, token);
    }

    private AuthResponse createAuthResponse(
            User user,
            String token
    ) {
        // build the shared response format used by both providers.
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