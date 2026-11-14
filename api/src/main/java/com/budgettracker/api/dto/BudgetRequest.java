package com.budgettracker.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * this is used so that once users submit their information on the first page then it will submit the
 * paycheck and the expenses together rather than in multiple API requests
 * @param paycheck paycheck request object
 * @param financials list of financials request object
 */
public record BudgetRequest(

        @NotNull
        @Valid
        PaycheckRequest paycheck,

        @NotNull
        @Valid
        List<FinancialsRequest> financials

) {}

