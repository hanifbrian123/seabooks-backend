package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.Vessel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VesselRepository extends JpaRepository<Vessel, Long> {
    List<Vessel> findAllByOrderByNameAsc();
}

