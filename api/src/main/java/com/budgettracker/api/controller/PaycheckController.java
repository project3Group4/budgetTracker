package com.budgettracker.api.controller;

import com.budgettracker.api.dto.PaycheckRequest;
import com.budgettracker.api.dto.PaycheckResponse;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.service.PaycheckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/me/paycheck")
public class PaycheckController {
    private final PaycheckService paycheckService;
    public PaycheckController(PaycheckService paycheckService) {
        this.paycheckService = paycheckService;
    }
    @GetMapping
    public PaycheckResponse getPaycheck(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return paycheckService.getPaycheck(userId);
    }
    @PutMapping
    public ResponseEntity<PaycheckResponse> savePaycheck(@AuthenticationPrincipal Jwt jwt,
                                                         @Valid @RequestBody PaycheckRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());

        PaycheckService.PaycheckSaveResult result = paycheckService.savePaycheckWithResult(userId, request);

        Paycheck paycheck = result.paycheck();
        PaycheckResponse response = new PaycheckResponse(paycheck.getId(), paycheck.getPaycheckAmount(),
                paycheck.getSavingsPercent());

        if (result.created()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.ok(response);
    }
}