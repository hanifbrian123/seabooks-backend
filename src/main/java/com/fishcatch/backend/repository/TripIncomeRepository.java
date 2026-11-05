package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.TripIncome;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripIncomeRepository extends JpaRepository<TripIncome, Long> {
    List<TripIncome> findByTripIdOrderByTransactionDateDescIdDesc(Long tripId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM TripIncome i WHERE i.trip.id = :tripId")
    BigDecimal sumAmountByTripId(@Param("tripId") Long tripId);

    void deleteByTripId(Long tripId);
}

