package com.budgettracker.api.auth;

/**
 * holds the google sign-in data sent by the expo app.
 *
 * <p>web sends an authorization code with pkce values, while android sends
 * a google id token from the native google sign-in library.</p>
 */
public record GoogleAuthRequest(
        String idToken,
        String code,
        String codeVerifier,
        String redirectUri
) {
    /**
     * checks whether the request contains a native google id token.
     */
    public boolean hasIdToken() {
        return idToken != null && !idToken.isBlank();
    }

    /**
     * checks whether the request contains all values needed for web code exchange.
     */
    public boolean hasAuthorizationCode() {
        return code != null
                && !code.isBlank()
                && codeVerifier != null
                && !codeVerifier.isBlank()
                && redirectUri != null
                && !redirectUri.isBlank();
    }
}