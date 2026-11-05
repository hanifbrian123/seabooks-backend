package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.VesselRequest;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.Vessel;
import com.fishcatch.backend.model.VesselStatus;
import com.fishcatch.backend.repository.VesselRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VesselService {

    private final VesselRepository vesselRepository;

    @Transactional(readOnly = true)
    public List<Vessel> findAll() {
        return vesselRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Vessel findById(Long id) {
        return vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel not found with id: " + id));
    }

    @Transactional
    public Vessel create(VesselRequest request) {
        Vessel vessel = Vessel.builder()
                .name(request.name().trim())
                .registrationNumber(request.registrationNumber() != null ? request.registrationNumber().trim() : null)
                .status(request.status() != null ? request.status() : VesselStatus.ACTIVE)
                .notes(request.notes())
                .build();
        return vesselRepository.save(vessel);
    }

    @Transactional
    public Vessel update(Long id, VesselRequest request) {
        Vessel vessel = findById(id);
        vessel.setName(request.name().trim());
        vessel.setRegistrationNumber(request.registrationNumber() != null ? request.registrationNumber().trim() : null);
        if (request.status() != null) {
            vessel.setStatus(request.status());
        }
        vessel.setNotes(request.notes());
        return vesselRepository.save(vessel);
    }

    @Transactional
    public void delete(Long id) {
        Vessel vessel = findById(id);
        vesselRepository.delete(vessel);
    }
}

