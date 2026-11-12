package com.budgettracker.api.controller;

import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.service.FinancialsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/me/paycheck/financials")
public class FinancialsController {
    private final FinancialsService financialsService;
    public FinancialsController(FinancialsService financialsService) {
        this.financialsService = financialsService;
    }

    @GetMapping
    public List<FinancialsResponse> getFinancials(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());

        return financialsService.getFinancials(userId);
    }
}