package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.Vessel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VesselRepository extends JpaRepository<Vessel, Long> {
    List<Vessel> findByUserIdOrderByNameAsc(Long userId);

    Optional<Vessel> findByIdAndUserId(Long id, Long userId);

    List<Vessel> findByUserIsNull();
}
