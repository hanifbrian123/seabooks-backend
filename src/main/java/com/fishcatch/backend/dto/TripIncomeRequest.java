package com.fishcatch.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TripIncomeRequest(
        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount
) {
}

