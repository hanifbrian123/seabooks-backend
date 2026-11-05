package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.CrewMember;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    List<CrewMember> findByUserIdOrderByNameAsc(Long userId);

    List<CrewMember> findByUserIdAndTotalKasbonGreaterThanOrderByNameAsc(Long userId, BigDecimal amount);

    Optional<CrewMember> findByIdAndUserId(Long id, Long userId);

    List<CrewMember> findByUserIsNull();
}
