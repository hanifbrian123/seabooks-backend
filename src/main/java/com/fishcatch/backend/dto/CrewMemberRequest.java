package com.fishcatch.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CrewMemberRequest(
        @NotBlank(message = "Crew member name is required")
        String name,
        @NotBlank(message = "Role is required")
        String role,
        String phone,
        Boolean active
) {
}
