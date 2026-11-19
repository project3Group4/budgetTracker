package com.budgettracker.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoogleIdentityClientTest {

    private static final String WEB_CLIENT_ID =
            "test-google-web-client-id";

    private static final String WEB_CLIENT_SECRET =
            "test-google-web-client-secret";

    private RestClient.Builder restClientBuilder;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClientBuilder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);

        when(restClientBuilder.build())
                .thenReturn(restClient);
    }

    @Test
    void verifiesValidGoogleIdToken() {
        Jwt jwt = createJwt(
                "google-user-123",
                "user@example.com",
                "Ann Jojo"
        );

        JwtDecoder decoder = token -> {
            assertEquals("valid-token", token);
            return jwt;
        };

        GoogleIdentityClient client = createClient(decoder);

        GoogleIdentityClient.VerifiedGoogleUser result =
                client.verify(
                        new GoogleAuthRequest(
                                "valid-token",
                                null,
                                null,
                                null
                        )
                );

        assertEquals(
                "google-user-123",
                result.providerUserId()
        );

        assertEquals(
                "user@example.com",
                result.email()
        );

        assertEquals(
                "Ann Jojo",
                result.displayName()
        );
    }

    @Test
    void usesEmailWhenGoogleDoesNotProvideDisplayName() {
        Jwt jwt = createJwt(
                "google-user-123",
                "user@example.com",
                null
        );

        GoogleIdentityClient client =
                createClient(token -> jwt);

        GoogleIdentityClient.VerifiedGoogleUser result =
                client.verify(
                        new GoogleAuthRequest(
                                "valid-token",
                                null,
                                null,
                                null
                        )
                );

        assertEquals(
                "user@example.com",
                result.displayName()
        );
    }

    @Test
    void rejectsRequestWithoutIdTokenOrAuthorizationCode() {
        GoogleIdentityClient client =
                createClient(token -> {
                    throw new AssertionError(
                            "decoder should not be called"
                    );
                });

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> client.verify(
                                new GoogleAuthRequest(
                                        null,
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    @Test
    void rejectsTokenWithWrongIssuer() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("iss", "https://evil.example.com")
                .audience(List.of(WEB_CLIENT_ID))
                .claim("email_verified", true)
                .subject("google-user-123")
                .claim("email", "user@example.com")
                .claim("name", "Ann Jojo")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        GoogleIdentityClient client =
                createClient(token -> jwt);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> client.verify(
                                new GoogleAuthRequest(
                                        "invalid-issuer-token",
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    @Test
    void rejectsTokenWithWrongAudience() {
        Jwt jwt = createJwtWithAudience(
                "google-user-123",
                "user@example.com",
                "Ann Jojo",
                "different-client-id"
        );

        GoogleIdentityClient client =
                createClient(token -> jwt);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> client.verify(
                                new GoogleAuthRequest(
                                        "wrong-audience-token",
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    @Test
    void rejectsUnverifiedEmail() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("iss", "https://accounts.google.com")
                .audience(List.of(WEB_CLIENT_ID))
                .claim("email_verified", false)
                .subject("google-user-123")
                .claim("email", "user@example.com")
                .claim("name", "Ann Jojo")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        GoogleIdentityClient client =
                createClient(token -> jwt);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> client.verify(
                                new GoogleAuthRequest(
                                        "unverified-email-token",
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    @Test
    void rejectsTokenWithoutRequiredIdentityValues() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("iss", "https://accounts.google.com")
                .audience(List.of(WEB_CLIENT_ID))
                .claim("email_verified", true)
                .subject("")
                .claim("email", "user@example.com")
                .claim("name", "Ann Jojo")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        GoogleIdentityClient client =
                createClient(token -> jwt);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> client.verify(
                                new GoogleAuthRequest(
                                        "missing-subject-token",
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    private GoogleIdentityClient createClient(
            JwtDecoder decoder
    ) {
        return new GoogleIdentityClient(
                restClientBuilder,
                WEB_CLIENT_ID,
                WEB_CLIENT_SECRET,
                decoder
        );
    }

    private Jwt createJwt(
            String subject,
            String email,
            String displayName
    ) {
        return createJwtWithAudience(
                subject,
                email,
                displayName,
                WEB_CLIENT_ID
        );
    }

    private Jwt createJwtWithAudience(
            String subject,
            String email,
            String displayName,
            String audience
    ) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("iss", "https://accounts.google.com")
                .audience(List.of(audience))
                .claim("email_verified", true)
                .subject(subject)
                .claim("email", email)
                .claim("name", displayName)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}