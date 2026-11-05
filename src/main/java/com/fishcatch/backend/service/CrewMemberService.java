package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.CrewMemberRequest;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.repository.CrewMemberRepository;
import com.fishcatch.backend.security.CurrentUserContext;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrewMemberService {

    private final CrewMemberRepository crewMemberRepository;
    private final CurrentUserContext currentUserContext;

    @Transactional(readOnly = true)
    public List<CrewMember> findAll() {
        Long userId = currentUserContext.getCurrentUserId();
        return crewMemberRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<CrewMember> findWithActiveDebt() {
        Long userId = currentUserContext.getCurrentUserId();
        return crewMemberRepository.findByUserIdAndTotalKasbonGreaterThanOrderByNameAsc(userId, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public CrewMember findById(Long id) {
        Long userId = currentUserContext.getCurrentUserId();
        return crewMemberRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member not found with id: " + id));
    }

    @Transactional
    public CrewMember create(CrewMemberRequest request) {
        User currentUser = currentUserContext.getCurrentUser();
        CrewMember crew = CrewMember.builder()
                .user(currentUser)
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
