package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByIsDemoTrueAndDemoExpiresAtBefore(LocalDateTime threshold);
}
