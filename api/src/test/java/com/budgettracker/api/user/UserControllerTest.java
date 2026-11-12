package com.budgettracker.api.user;

import com.budgettracker.api.entity.Role;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.UserRepository;
import com.budgettracker.api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void validJwtReturnsCurrentUser() throws Exception {
        // create a fake user returned by the mocked repository
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("octocat@example.com");
        when(user.getDisplayName()).thenReturn("octocat");
        when(user.getRole()).thenReturn(Role.USER);

        // return the fake user when the controller looks up user id 1
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        // send a request with a fake jwt containing user id 1
        mockMvc.perform(
                        get("/api/v1/user/me")
                                .with(jwt().jwt(token ->
                                        token.subject("1")))
                )
                // confirm that the protected endpoint accepts the jwt
                .andExpect(status().isOk())
                // confirm that the endpoint returns the correct user data
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("octocat@example.com"))
                .andExpect(jsonPath("$.displayName")
                        .value("octocat"))
                .andExpect(jsonPath("$.role")
                        .value("USER"));
    }

    @Test
    void missingJwtReturnsUnauthorized() throws Exception {
        // send the request without an authorization header
        mockMvc.perform(
                        get("/api/v1/user/me")
                )
                // confirm that spring security blocks the request
                .andExpect(status().isUnauthorized());
    }
}