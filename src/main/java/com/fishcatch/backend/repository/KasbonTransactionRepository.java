package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.KasbonTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KasbonTransactionRepository extends JpaRepository<KasbonTransaction, Long> {
    List<KasbonTransaction> findAllByOrderByTransactionDateDescIdDesc();

    List<KasbonTransaction> findByCrewMemberIdOrderByTransactionDateDescIdDesc(Long crewMemberId);

    Optional<KasbonTransaction> findByTripExpenseId(Long tripExpenseId);
}
