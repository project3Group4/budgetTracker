package com.budgettracker.api.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * holds the sign-in information the app sends after github redirects back to it.
 *
 * @param code the temporary code github returned to the app
 * @param codeVerifier the proof the app created before opening github
 * @param redirectUri the address github sent the user back to
 */
public record GitHubAuthRequest(
        @NotBlank String code,
        @NotBlank String codeVerifier,
        @NotBlank String redirectUri
) {
}