package com.budgettracker.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleIdentityClientTest {

    private JwtDecoder jwtDecoder;
    private GoogleIdentityClient googleIdentityClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        // create a fake jwt decoder so the tests do not contact google.
        jwtDecoder = mock(JwtDecoder.class);

        // connect a mock server to the rest client used for web code exchange.
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        // create the google client with test credentials and the fake decoder.
        googleIdentityClient = new GoogleIdentityClient(
                builder,
                "test-web-client-id",
                "test-web-client-secret",
                jwtDecoder
        );
    }

    @Test
    void verifiesNativeGoogleIdToken() {
        // create the claims expected from a verified google account.
        Jwt jwt = createValidJwt();

        // make the fake decoder return the test claims.
        when(jwtDecoder.decode("test-id-token")).thenReturn(jwt);

        // send the native android request containing only an id token.
        GoogleIdentityClient.VerifiedGoogleUser result =
                googleIdentityClient.verify(
                        new GoogleAuthRequest(
                                "test-id-token",
                                null,
                                null,
                                null
                        )
                );

        // confirm that the verified google identity was returned.
        assertEquals("google-user-123", result.providerUserId());
        assertEquals("verified@example.com", result.email());
        assertEquals("Test User", result.displayName());
    }

    @Test
    void rejectsGoogleAccountWithUnverifiedEmail() {
        // create claims for an account whose email is not verified.
        Jwt jwt = Jwt.withTokenValue("test-id-token")
                .header("alg", "RS256")
                .claim("iss", "https://accounts.google.com")
                .claim("aud", List.of("test-web-client-id"))
                .subject("google-user-123")
                .claim("email", "unverified@example.com")
                .claim("email_verified", false)
                .claim("name", "Test User")
                .build();

        // make the fake decoder return the unverified account claims.
        when(jwtDecoder.decode("test-id-token")).thenReturn(jwt);

        // confirm that unverified email accounts are rejected.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> googleIdentityClient.verify(
                        new GoogleAuthRequest(
                                "test-id-token",
                                null,
                                null,
                                null
                        )
                )
        );

        // confirm that the api returns unauthorized for the rejected login.
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void exchangesWebAuthorizationCodeForIdToken() {
        // configure the fake google token endpoint response.
        server.expect(
                        requestTo("https://oauth2.googleapis.com/token")
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                "{\"id_token\":\"test-id-token\"}",
                                MediaType.APPLICATION_JSON
                        )
                );

        // make the fake decoder return valid claims for the exchanged token.
        when(jwtDecoder.decode("test-id-token"))
                .thenReturn(createValidJwt());

        // send the web request containing the authorization code and pkce values.
        GoogleIdentityClient.VerifiedGoogleUser result =
                googleIdentityClient.verify(
                        new GoogleAuthRequest(
                                null,
                                "test-code",
                                "test-code-verifier",
                                "http://localhost:8081/auth"
                        )
                );

        // confirm that the exchanged token produced a verified identity.
        assertEquals("google-user-123", result.providerUserId());
        assertEquals("verified@example.com", result.email());

        // confirm that the expected google request was made.
        server.verify();
    }

    @Test
    void rejectsRequestWithNeitherSupportedGoogleFormat() {
        // confirm that an empty request is rejected.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> googleIdentityClient.verify(
                        new GoogleAuthRequest(
                                null,
                                null,
                                null,
                                null
                        )
                )
        );

        // confirm that the api returns unauthorized for invalid input.
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    private Jwt createValidJwt() {
        // create claims for a verified google account using the configured client id.
        return Jwt.withTokenValue("test-id-token")
                .header("alg", "RS256")
                .claim("iss", "https://accounts.google.com")
                .claim("aud", List.of("test-web-client-id"))
                .subject("google-user-123")
                .claim("email", "verified@example.com")
                .claim("email_verified", true)
                .claim("name", "Test User")
                .build();
    }
}