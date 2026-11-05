package com.fishcatch.backend.repository;

import com.fishcatch.backend.model.TripExpense;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripExpenseRepository extends JpaRepository<TripExpense, Long> {
    List<TripExpense> findByTripIdOrderByTransactionDateDescIdDesc(Long tripId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM TripExpense e WHERE e.trip.id = :tripId")
    BigDecimal sumAmountByTripId(@Param("tripId") Long tripId);
}

