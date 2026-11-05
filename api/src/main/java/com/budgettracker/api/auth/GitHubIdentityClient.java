package com.budgettracker.api.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GitHubIdentityClient {
    private static final String TOKEN_URL =
            "https://github.com/login/oauth/access_token";
    private static final String USER_URL =
            "https://api.github.com/user";
    private static final String EMAILS_URL =
            "https://api.github.com/user/emails";
    private static final MediaType GITHUB_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.github+json");

    // this client makes requests from the backend to github.
    private final RestClient githubClient;

    // these values come from application.properties and the environment.
    private final String clientId;
    private final String clientSecret;

    // builds a github client for real requests in the app and fake replies in tests.
    public GitHubIdentityClient(
            RestClient.Builder githubClientBuilder,
            @Value("${app.oauth.github.client-id}") String clientId,
            @Value("${app.oauth.github.client-secret}") String clientSecret) {
        this.githubClient = githubClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    /**
     * trades the temporary code for github account details.
     *
     * @param request the sign-in values sent by the app
     * @return the github user id, username, and verified email
     */
    public VerifiedGitHubUser exchangeCodeForIdentity(
            GitHubAuthRequest request) {
        try {
            // github needs these values to exchange the code for an access token.
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("code", request.code());
            form.add("code_verifier", request.codeVerifier());
            form.add("redirect_uri", request.redirectUri());

            // the backend keeps the access token and uses it to ask github who signed in.
            GitHubTokenResponse token = githubClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(GitHubTokenResponse.class);

            if (token == null || token.accessToken() == null
                    || token.accessToken().isBlank()) {
                throw signInRejected();
            }

            String bearerToken = "Bearer " + token.accessToken();

            // github returns the account id and username for this access token.
            GitHubUserResponse user = githubClient.get()
                    .uri(USER_URL)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .accept(GITHUB_MEDIA_TYPE)
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(GitHubUserResponse.class);

            if (user == null || user.id() == null || user.login() == null) {
                throw signInRejected();
            }

            // the email list is needed because github may keep the user's email private.
            List<GitHubEmailResponse> emails = githubClient.get()
                    .uri(EMAILS_URL)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .accept(GITHUB_MEDIA_TYPE)
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (emails == null) {
                throw signInRejected();
            }

            // only use an email github marks as both primary and verified.
            GitHubEmailResponse primaryEmail = emails.stream()
                    .filter(email -> email.primary() && email.verified())
                    .findFirst()
                    .orElseThrow(GitHubIdentityClient::signInRejected);

            return new VerifiedGitHubUser(
                    String.valueOf(user.id()),
                    user.login(),
                    primaryEmail.email());
        } catch (RestClientResponseException exception) {
            // do not show github's technical response details to the user.
            HttpStatus status = exception.getStatusCode().is4xxClientError()
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.BAD_GATEWAY;
            throw new ResponseStatusException(
                    status, "GitHub could not verify this sign-in.", exception);
        }
    }

    /**
     * makes one clear error for sign-ins that do not provide the required data.
     */
    private static ResponseStatusException signInRejected() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "GitHub sign-in needs a valid account with a verified primary email.");
    }

    /**
     * holds the account details github returned after verification.
     */
    public record VerifiedGitHubUser(
            String providerUserId,
            String username,
            String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubTokenResponse(
            @JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubUserResponse(Long id, String login) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubEmailResponse(
            String email,
            boolean primary,
            boolean verified) {
    }
}