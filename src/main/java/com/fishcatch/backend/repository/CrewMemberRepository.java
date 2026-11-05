package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.CrewMember;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    List<CrewMember> findAllByOrderByNameAsc();

    List<CrewMember> findByTotalKasbonGreaterThanOrderByNameAsc(BigDecimal amount);
}
