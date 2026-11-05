package com.fishcatch.backend.dto;

import com.fishcatch.backend.model.TripStatus;
import com.fishcatch.backend.model.Vessel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        Vessel vessel,
        LocalDate departureDate,
        LocalDate estimatedReturnDate,
        TripStatus status,
        String notes,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netProfit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
