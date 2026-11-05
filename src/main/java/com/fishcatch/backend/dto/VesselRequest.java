package com.fishcatch.backend.dto;

import com.fishcatch.backend.model.VesselStatus;
import jakarta.validation.constraints.NotBlank;

public record VesselRequest(
        @NotBlank(message = "Vessel name is required")
        String name,
        String registrationNumber,
        VesselStatus status,
        String notes
) {
}

