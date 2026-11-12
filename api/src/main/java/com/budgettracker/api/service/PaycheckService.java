package com.budgettracker.api.service;

import com.budgettracker.api.dto.PaycheckRequest;
import com.budgettracker.api.dto.PaycheckResponse;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.entity.User;
import com.budgettracker.api.repository.PaycheckRepository;
import com.budgettracker.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaycheckService {
    private final PaycheckRepository paycheckRepository;
    private final UserRepository userRepository;

    public PaycheckService(PaycheckRepository paycheckRepository, UserRepository userRepository) {
        this.paycheckRepository = paycheckRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    //gets user's existing paycheck and converts to paycheck response dto
    public PaycheckResponse getPaycheck(Long userId) {
        Paycheck paycheck = findPaycheck(userId);

        return new PaycheckResponse(
                paycheck.getId(),
                paycheck.getPaycheckAmount(),
                paycheck.getSavingsPercent()
        );
    }

    @Transactional(readOnly = true)
    //retrieves actual paycheck entity from db
    public Paycheck findPaycheck(Long userId) {
        return paycheckRepository.findByUserId(userId) .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Paycheck not found"));
    }

    @Transactional
    //handles first time paycheck and all subsequent ones
    public Paycheck savePaycheck(Long userId, PaycheckRequest request) {
        return savePaycheckWithResult(userId, request).paycheck();
    }

    @Transactional
    public PaycheckSaveResult savePaycheckWithResult(Long userId, PaycheckRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));

        Paycheck paycheck = paycheckRepository.findByUserId(userId).orElse(null);

        boolean created = paycheck == null;

        if (created) {
            paycheck = new Paycheck(user, request.paycheckAmount(), request.savingsPercent());
        } else {
            paycheck.updatePaycheck(request.paycheckAmount(), request.savingsPercent());
        }
        Paycheck savedPaycheck = paycheckRepository.save(paycheck);

        return new PaycheckSaveResult(savedPaycheck, created);
    }

    //allows us to return both saved paycheck and whether its a new one
    public record PaycheckSaveResult(Paycheck paycheck, boolean created){
    }
}