package com.fishcatch.backend.security;

import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private final UserRepository userRepository;

    public void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public Long getCurrentUserId() {
        Long id = CURRENT_USER_ID.get();
        if (id == null) {
            throw new IllegalStateException("Authentication is required");
        }
        return id;
    }

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    public void clear() {
        CURRENT_USER_ID.remove();
    }
}

