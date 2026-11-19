package com.budgettracker.api.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GoogleIdentityClient {

    private static final String GOOGLE_TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    private static final String GOOGLE_JWK_URL =
            "https://www.googleapis.com/oauth2/v3/certs";

    private final RestClient googleClient;
    private final JwtDecoder googleJwtDecoder;
    private final String webClientId;
    private final String webClientSecret;

    @Autowired
    public GoogleIdentityClient(
            RestClient.Builder googleClientBuilder,
            @Value("${app.oauth.google.web-client-id}") String webClientId,
            @Value("${app.oauth.google.web-client-secret}") String webClientSecret
    ) {
        // create the decoder used by the real application.
        this(
                googleClientBuilder,
                webClientId,
                webClientSecret,
                NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWK_URL).build()
        );
    }

    GoogleIdentityClient(
            RestClient.Builder googleClientBuilder,
            String webClientId,
            String webClientSecret,
            JwtDecoder googleJwtDecoder
    ) {
        // create the client used to communicate with google.
        this.googleClient = googleClientBuilder.build();

        // store the google web client values used during the web code exchange.
        this.webClientId = webClientId;
        this.webClientSecret = webClientSecret;

        // use the supplied decoder so unit tests can provide a fake decoder.
        this.googleJwtDecoder = googleJwtDecoder;
    }

    public VerifiedGoogleUser verify(GoogleAuthRequest request) {
        // check which google sign-in format the request contains.
        boolean hasIdToken = request.hasIdToken();
        boolean hasAuthorizationCode = request.hasAuthorizationCode();

        // reject requests that contain neither format or both formats.
        if (hasIdToken == hasAuthorizationCode) {
            throw signInRejected();
        }

        try {
            // use the native id token when android sends one.
            String idToken = hasIdToken
                    ? request.idToken()
                    : exchangeWebCode(request);

            // verify the token signature and standard jwt timestamps.
            Jwt jwt = googleJwtDecoder.decode(idToken);

            // verify that google issued this token.
            String issuer = jwt.getClaimAsString("iss");

            if (!"https://accounts.google.com".equals(issuer)
                    && !"accounts.google.com".equals(issuer)) {
                throw signInRejected();
            }

            // verify that the token was created for the team's web client.
            List<String> audiences = jwt.getAudience();

            if (audiences == null || !audiences.contains(webClientId)) {
                throw signInRejected();
            }

            // only accept google accounts with verified email addresses.
            Boolean emailVerified =
                    jwt.getClaimAsBoolean("email_verified");

            if (!Boolean.TRUE.equals(emailVerified)) {
                throw signInRejected();
            }

            // read google's stable id, email, and display name.
            String providerUserId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String displayName = jwt.getClaimAsString("name");

            // reject tokens that do not contain the required identity values.
            if (providerUserId == null
                    || providerUserId.isBlank()
                    || email == null
                    || email.isBlank()) {
                throw signInRejected();
            }

            // use the email when google does not provide a display name.
            if (displayName == null || displayName.isBlank()) {
                displayName = email;
            }

            // return the verified identity information to the auth service.
            return new VerifiedGoogleUser(
                    providerUserId,
                    email,
                    displayName
            );
        } catch (JwtException exception) {
            // hide token verification details from the client.
            throw signInRejected();
        } catch (RestClientResponseException exception) {
            // hide google's technical response details from the client.
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google could not verify this sign-in.",
                    exception
            );
        }
    }

    private String exchangeWebCode(GoogleAuthRequest request) {
        // create the values google needs to exchange the web code.
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("client_id", webClientId);
        form.add("client_secret", webClientSecret);
        form.add("code", request.code());
        form.add("code_verifier", request.codeVerifier());
        form.add("redirect_uri", request.redirectUri());
        form.add("grant_type", "authorization_code");

        // send the web authorization code to google's token endpoint.
        GoogleTokenResponse response = googleClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);

        // reject the response if google did not return an id token.
        if (response == null
                || response.idToken() == null
                || response.idToken().isBlank()) {
            throw signInRejected();
        }

        // return the id token so the caller can verify it.
        return response.idToken();
    }

    private static ResponseStatusException signInRejected() {
        // use one safe error message for invalid google sign-ins.
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Google sign-in needs a valid account with a verified email."
        );
    }

    public record VerifiedGoogleUser(
            String providerUserId,
            String email,
            String displayName
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTokenResponse(
            @JsonProperty("id_token") String idToken
    ) {
    }
}