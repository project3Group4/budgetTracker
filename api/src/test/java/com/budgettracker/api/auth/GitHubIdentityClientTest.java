package com.budgettracker.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubIdentityClientTest {
    private MockRestServiceServer server;
    private GitHubIdentityClient githubIdentityClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        githubIdentityClient = new GitHubIdentityClient(
                builder,
                "test-client-id",
                "test-client-secret");
    }

    // checks that github identity is returned with the verified primary email.
    @Test
    void returnsGitHubIdentityUsingVerifiedPrimaryEmail() {
        server.expect(requestTo("https://github.com/login/oauth/access_token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"test-access-token\"}",
                        MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.github.com/user"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":42,\"login\":\"octocat\"}",
                        MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.github.com/user/emails"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        [
                          {"email":"not-verified@example.com","primary":true,"verified":false},
                          {"email":"verified@example.com","primary":true,"verified":true}
                        ]
                        """,
                        MediaType.APPLICATION_JSON));

        GitHubIdentityClient.VerifiedGitHubUser result =
                githubIdentityClient.exchangeCodeForIdentity(
                        new GitHubAuthRequest(
                                "test-code",
                                "test-verifier",
                                "http://localhost:8081/auth"));

        assertEquals("42", result.providerUserId());
        assertEquals("octocat", result.username());
        assertEquals("verified@example.com", result.email());
        server.verify();
    }

    // checks that sign-in is rejected when github has no verified primary email.
    @Test
    void rejectsGitHubAccountWithoutVerifiedPrimaryEmail() {
        server.expect(requestTo("https://github.com/login/oauth/access_token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"test-access-token\"}",
                        MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.github.com/user"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":42,\"login\":\"octocat\"}",
                        MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.github.com/user/emails"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        [
                          {"email":"not-verified@example.com","primary":true,"verified":false}
                        ]
                        """,
                        MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> githubIdentityClient.exchangeCodeForIdentity(
                        new GitHubAuthRequest(
                                "test-code",
                                "test-verifier",
                                "http://localhost:8081/auth")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(exception.getReason().contains("verified primary email"));
        server.verify();
    }
}