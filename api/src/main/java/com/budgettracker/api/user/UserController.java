package com.budgettracker.api.user;

import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for user API routes
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Default constructor
     * @param userRepository user repository class
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     *
     * @param jwt authentication principal
     * @return new user response
     */
    @GetMapping("/me")
    public UserResponse getMe(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().name()
        );
    }

    public record UserResponse(
            Long id,
            String email,
            String displayName,
            String role
    ) {
    }
}