package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.TripExpenseRequest;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.model.KasbonTransaction;
import com.fishcatch.backend.model.Trip;
import com.fishcatch.backend.model.TripExpense;
import com.fishcatch.backend.repository.TripExpenseRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripExpenseService {

    private final TripExpenseRepository tripExpenseRepository;
    private final TripService tripService;
    private final CrewMemberService crewMemberService;
    private final KasbonTransactionService kasbonTransactionService;

    @Transactional(readOnly = true)
    public List<TripExpense> findByTripId(Long tripId) {
        tripService.getTripEntity(tripId);
        return tripExpenseRepository.findByTripIdOrderByTransactionDateDescIdDesc(tripId);
    }

    @Transactional
    public TripExpense create(Long tripId, TripExpenseRequest request) {
        Trip trip = tripService.getTripEntity(tripId);
        boolean deduct = Boolean.TRUE.equals(request.hasKasbonDeduction());

        if (deduct) {
            validateDeductionRequest(request);
            CrewMember crew = crewMemberService.findById(request.crewMemberId());
            BigDecimal deduction = request.kasbonDeductionAmount();
            BigDecimal netPaid = request.amount().subtract(deduction);

            TripExpense expense = TripExpense.builder()
                    .trip(trip)
                    .transactionDate(request.transactionDate())
                    .category(request.category())
                    .amount(request.amount())
                    .description(request.description().trim())
                    .hasKasbonDeduction(true)
                    .crewMember(crew)
                    .kasbonDeductionAmount(deduction)
                    .netPaidAmount(netPaid)
                    .build();
            TripExpense saved = tripExpenseRepository.save(expense);

            String kasbonNote = buildKasbonDescription(trip, saved.getDescription());
            KasbonTransaction syncedTx = kasbonTransactionService.syncFromTripExpense(
                    saved.getId(),
                    crew.getId(),
                    saved.getTransactionDate(),
                    deduction,
                    kasbonNote
            );
            saved.setCrewMember(syncedTx.getCrewMember());
            return saved;
        } else {
            TripExpense expense = TripExpense.builder()
                    .trip(trip)
                    .transactionDate(request.transactionDate())
                    .category(request.category())
                    .amount(request.amount())
                    .description(request.description().trim())
                    .hasKasbonDeduction(false)
                    .crewMember(null)
                    .kasbonDeductionAmount(BigDecimal.ZERO)
                    .netPaidAmount(request.amount())
                    .build();
            return tripExpenseRepository.save(expense);
        }
    }

    @Transactional
    public TripExpense update(Long tripId, Long expenseId, TripExpenseRequest request) {
        Trip trip = tripService.getTripEntity(tripId);
        TripExpense existing = getExpenseForTrip(tripId, expenseId);
        boolean deduct = Boolean.TRUE.equals(request.hasKasbonDeduction());

        existing.setTransactionDate(request.transactionDate());
        existing.setCategory(request.category());
        existing.setAmount(request.amount());
        existing.setDescription(request.description().trim());

        if (deduct) {
            validateDeductionRequest(request);
            BigDecimal deduction = request.kasbonDeductionAmount();
            BigDecimal netPaid = request.amount().subtract(deduction);

            String kasbonNote = buildKasbonDescription(trip, existing.getDescription());
            KasbonTransaction syncedTx = kasbonTransactionService.syncFromTripExpense(
                    existing.getId(),
                    request.crewMemberId(),
                    request.transactionDate(),
                    deduction,
                    kasbonNote
            );

            existing.setHasKasbonDeduction(true);
            existing.setCrewMember(syncedTx.getCrewMember());
            existing.setKasbonDeductionAmount(deduction);
            existing.setNetPaidAmount(netPaid);
        } else {
            kasbonTransactionService.deleteByTripExpenseId(existing.getId());
            existing.setHasKasbonDeduction(false);
            existing.setCrewMember(null);
            existing.setKasbonDeductionAmount(BigDecimal.ZERO);
            existing.setNetPaidAmount(request.amount());
        }

        return tripExpenseRepository.save(existing);
    }

    @Transactional
    public void delete(Long tripId, Long expenseId) {
        TripExpense existing = getExpenseForTrip(tripId, expenseId);
        kasbonTransactionService.deleteByTripExpenseId(existing.getId());
        tripExpenseRepository.delete(existing);
    }

    private TripExpense getExpenseForTrip(Long tripId, Long expenseId) {
        TripExpense expense = tripExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip expense not found with id: " + expenseId));
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense record does not belong to trip id: " + tripId);
        }
        return expense;
    }

    private void validateDeductionRequest(TripExpenseRequest request) {
        if (request.crewMemberId() == null) {
            throw new IllegalArgumentException("Crew member must be selected when kasbon deduction is enabled");
        }
        if (request.kasbonDeductionAmount() == null
                || request.kasbonDeductionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Kasbon deduction amount must be greater than zero");
        }
        if (request.kasbonDeductionAmount().compareTo(request.amount()) > 0) {
            throw new IllegalArgumentException(
                    "Kasbon deduction amount cannot exceed the original expense/wage amount"
            );
        }
    }

    private String buildKasbonDescription(Trip trip, String expenseDescription) {
        return String.format(
                "Wage deduction on Trip #%d (%s) — %s",
                trip.getId(),
                trip.getVessel().getName(),
                expenseDescription
        );
    }
}

