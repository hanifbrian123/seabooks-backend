package com.fishcatch.backend.dto;

import com.fishcatch.backend.model.TripStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TripRequest(
        @NotNull(message = "Vessel is required")
        Long vesselId,
        @NotNull(message = "Departure date is required")
        LocalDate departureDate,
        @NotNull(message = "Estimated return date is required")
        LocalDate estimatedReturnDate,
        TripStatus status,
        String notes
) {
}

