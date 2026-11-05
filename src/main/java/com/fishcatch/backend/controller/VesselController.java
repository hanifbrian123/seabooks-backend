package com.fishcatch.backend.controller;

import com.fishcatch.backend.dto.VesselRequest;
import com.fishcatch.backend.model.Vessel;
import com.fishcatch.backend.service.VesselService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vessels")
@RequiredArgsConstructor
public class VesselController {

    private final VesselService vesselService;

    @GetMapping
    public List<Vessel> getAllVessels() {
        return vesselService.findAll();
    }

    @GetMapping("/{id}")
    public Vessel getVesselById(@PathVariable Long id) {
        return vesselService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vessel createVessel(@Valid @RequestBody VesselRequest request) {
        return vesselService.create(request);
    }

    @PutMapping("/{id}")
    public Vessel updateVessel(@PathVariable Long id, @Valid @RequestBody VesselRequest request) {
        return vesselService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVessel(@PathVariable Long id) {
        vesselService.delete(id);
    }
}
