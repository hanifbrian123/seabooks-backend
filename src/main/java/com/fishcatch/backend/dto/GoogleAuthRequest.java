package com.fishcatch.backend.dto;

public record GoogleAuthRequest(
        String credential,
        String accessToken,
        String email,
        String name,
        String googleId,
        String avatarUrl
) {
}
