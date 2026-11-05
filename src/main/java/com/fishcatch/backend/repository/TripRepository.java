package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.Trip;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByUserIdOrderByDepartureDateDescIdDesc(Long userId);

    Optional<Trip> findByIdAndUserId(Long id, Long userId);

    List<Trip> findByUserIsNull();
}
