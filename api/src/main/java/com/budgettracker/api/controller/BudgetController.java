package com.budgettracker.api.controller;

import com.budgettracker.api.dto.BudgetRequest;
import com.budgettracker.api.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/me/budget")
public class BudgetController {
    private final BudgetService budgetService;
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    //creates or updates users budget
    //authenticated principal is used to identify signed in user
    @PutMapping
    public ResponseEntity<Void> saveBudget(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody BudgetRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());

        //saves all budget info
        budgetService.saveBudget(userId, request);

        //returns 204 no content when succeded
        return ResponseEntity.noContent().build();
    }
}