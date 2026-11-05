package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.CrewMemberRequest;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.repository.CrewMemberRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrewMemberService {

    private final CrewMemberRepository crewMemberRepository;

    @Transactional(readOnly = true)
    public List<CrewMember> findAll() {
        return crewMemberRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<CrewMember> findWithActiveDebt() {
        return crewMemberRepository.findByTotalKasbonGreaterThanOrderByNameAsc(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public CrewMember findById(Long id) {
        return crewMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member not found with id: " + id));
    }

    @Transactional
    public CrewMember create(CrewMemberRequest request) {
        CrewMember crew = CrewMember.builder()
                .name(request.name().trim())
                .role(request.role().trim())
                .phone(request.phone() != null ? request.phone().trim() : null)
                .totalKasbon(BigDecimal.ZERO)
                .active(request.active() != null ? request.active() : true)
                .build();
        return crewMemberRepository.save(crew);
    }

    @Transactional
    public CrewMember update(Long id, CrewMemberRequest request) {
        CrewMember crew = findById(id);
        crew.setName(request.name().trim());
        crew.setRole(request.role().trim());
        crew.setPhone(request.phone() != null ? request.phone().trim() : null);
        if (request.active() != null) {
            crew.setActive(request.active());
        }
        return crewMemberRepository.save(crew);
    }

    @Transactional
    public void delete(Long id) {
        CrewMember crew = findById(id);
        crewMemberRepository.delete(crew);
    }
}

