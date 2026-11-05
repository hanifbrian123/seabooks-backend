package com.fishcatch.backend.dto;

import com.fishcatch.backend.model.KasbonTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record KasbonTransactionRequest(
        @NotNull(message = "Crew member is required")
        Long crewMemberId,
        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,
        @NotNull(message = "Transaction type is required")
        KasbonTransactionType type,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,
        String description
) {
}
