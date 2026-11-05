package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.Trip;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByOrderByDepartureDateDescIdDesc();
}
