package com.budgettracker.api.controller;

import com.budgettracker.api.dto.BudgetRequest;
import com.budgettracker.api.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * The controller for the budget API route
 */
@RestController
@RequestMapping("/api/v1/user/me/budget")
public class BudgetController {
    private final BudgetService budgetService;

    /**
     * Constructor for the controller
     * @param budgetService service class
     */
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * creates or updates users budget
     * authenticated principal is used to identify signed-in user
     * @param jwt user authentication principal
     * @param request the request body for the budget
     * @return 204 no content when succeeded
     */
    @PutMapping
    public ResponseEntity<Void> saveBudget(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody BudgetRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());

        //saves all budget info
        budgetService.saveBudget(userId, request);

        return ResponseEntity.noContent().build();
    }
}