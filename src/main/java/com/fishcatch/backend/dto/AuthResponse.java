package com.fishcatch.backend.dto;

import com.fishcatch.backend.model.User;

public record AuthResponse(
        String token,
        User user
) {
}
