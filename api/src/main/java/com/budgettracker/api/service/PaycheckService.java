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

/**
 * Service class for paycheck controller
 */
@Service
public class PaycheckService {
    private final PaycheckRepository paycheckRepository;
    private final UserRepository userRepository;

    /**
     * Constructor for class
     * @param paycheckRepository Paycheck repository class
     * @param userRepository User repository class
     */
    public PaycheckService(PaycheckRepository paycheckRepository, UserRepository userRepository) {
        this.paycheckRepository = paycheckRepository;
        this.userRepository = userRepository;
    }

    /**
     * gets user's existing paycheck and converts to paycheck response dto
     * @param userId Long
     * @return paycheck response dto
     */
    @Transactional(readOnly = true)
    public PaycheckResponse getPaycheck(Long userId) {
        Paycheck paycheck = findPaycheck(userId);

        return new PaycheckResponse(
                paycheck.getId(),
                paycheck.getPaycheckAmount(),
                paycheck.getSavingsPercent()
        );
    }

    /**
     * retrieves actual paycheck entity from db
     * @param userId Long
     * @return paycheck entity
     */
    @Transactional(readOnly = true)
    public Paycheck findPaycheck(Long userId) {
        return paycheckRepository.findByUserId(userId) .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Paycheck not found"));
    }

    /**
     * handles first time paycheck and all subsequent ones
     * @param userId Long
     * @param request paycheck request
     * @return saved paycheck entity
     */
    @Transactional
    public Paycheck savePaycheck(Long userId, PaycheckRequest request) {
        return savePaycheckWithResult(userId, request).paycheck();
    }

    /**
     * Deletes paycheck belonging to user
     * @param userId Long
     */
    public void deletePaycheck(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        Paycheck paycheck = paycheckRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Paycheck not found"));
        paycheckRepository.delete(paycheck);
    }

    /**
     * Saves paycheck result
     * @param userId Long
     * @param request Paycheck request
     * @return returns saved or new paycheck
     */
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