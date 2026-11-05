package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.KasbonTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KasbonTransactionRepository extends JpaRepository<KasbonTransaction, Long> {
    List<KasbonTransaction> findByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    List<KasbonTransaction> findByUserIdAndCrewMemberIdOrderByTransactionDateDescIdDesc(
            Long userId,
            Long crewMemberId
    );

    Optional<KasbonTransaction> findByIdAndUserId(Long id, Long userId);

    Optional<KasbonTransaction> findByTripExpenseId(Long tripExpenseId);

    List<KasbonTransaction> findByUserIsNull();
}
