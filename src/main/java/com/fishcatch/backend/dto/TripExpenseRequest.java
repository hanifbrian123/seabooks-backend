package com.fishcatch.backend.dto;

import com.fishcatch.backend.model.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TripExpenseRequest(
        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,
        @NotNull(message = "Expense category is required")
        ExpenseCategory category,
        @NotNull(message = "Expense amount is required")
        @DecimalMin(value = "0.01", message = "Expense amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "Description is required")
        String description,
        Boolean hasKasbonDeduction,
        Long crewMemberId,
        BigDecimal kasbonDeductionAmount
) {
}
