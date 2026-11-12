package com.budgettracker.api.service;

import com.budgettracker.api.dto.FinancialsRequest;
import com.budgettracker.api.dto.FinancialsResponse;
import com.budgettracker.api.entity.Category;
import com.budgettracker.api.entity.Financials;
import com.budgettracker.api.entity.Paycheck;
import com.budgettracker.api.repository.FinancialsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialsService{
    private final FinancialsRepository financialsRepository;
    private final PaycheckService paycheckService;
    private final CategoryService categoryService;

    public FinancialsService(FinancialsRepository financialsRepository, PaycheckService paycheckService,
                             CategoryService categoryService){
        this.financialsRepository = financialsRepository;
        this.paycheckService = paycheckService;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    //retrieves all fianancial allocations saved w/ authenticated user's paycheck
    public List<FinancialsResponse> getFinancials(Long userId) {
        Paycheck paycheck = paycheckService.findPaycheck(userId);

        List<Financials> financials = financialsRepository.findByPaycheckId(paycheck.getId());

        List<FinancialsResponse> responses = new ArrayList<>();

        for (Financials financial : financials) {
            responses.add(new FinancialsResponse(financial.getId(), financial.getPaycheck().getId(),
                    financial.getCategory().getId(), financial.getSetAmount()));
        }

        return responses;
    }

    @Transactional
    //saves or updates an individual category's budget allocation
    public Financials saveFinancial(Paycheck paycheck, FinancialsRequest request){
        Category category = categoryService.getAvailableCategory(request.categoryId());

        Financials financial = financialsRepository.findByPaycheckIdAndCategoryId(paycheck.getId(),
                category.getId()).orElse(null);

        if(financial == null){
            financial = new Financials(paycheck, category, request.setAmount());
        }else{
            financial.updateSetAmount(request.setAmount());
        }

        return financialsRepository.save(financial);
    }

}