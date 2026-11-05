package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.TripRequest;
import com.fishcatch.backend.dto.TripResponse;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.Trip;
import com.fishcatch.backend.model.TripExpense;
import com.fishcatch.backend.model.TripStatus;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.model.Vessel;
import com.fishcatch.backend.repository.TripExpenseRepository;
import com.fishcatch.backend.repository.TripIncomeRepository;
import com.fishcatch.backend.repository.TripRepository;
import com.fishcatch.backend.security.CurrentUserContext;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final VesselService vesselService;
    private final TripIncomeRepository tripIncomeRepository;
    private final TripExpenseRepository tripExpenseRepository;
    private final KasbonTransactionService kasbonTransactionService;
    private final CurrentUserContext currentUserContext;

    @Transactional(readOnly = true)
    public List<TripResponse> findAll() {
        Long userId = currentUserContext.getCurrentUserId();
        return tripRepository.findByUserIdOrderByDepartureDateDescIdDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse findById(Long id) {
        Trip trip = getTripEntity(id);
        return toResponse(trip);
    }

    @Transactional(readOnly = true)
    public Trip getTripEntity(Long id) {
        Long userId = currentUserContext.getCurrentUserId();
        return tripRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
    }

    @Transactional
    public TripResponse create(TripRequest request) {
        validateDates(request);
        User currentUser = currentUserContext.getCurrentUser();
        Vessel vessel = vesselService.findById(request.vesselId());
        Trip trip = Trip.builder()
                .user(currentUser)
                .vessel(vessel)
                .departureDate(request.departureDate())
                .estimatedReturnDate(request.estimatedReturnDate())
                .status(request.status() != null ? request.status() : TripStatus.ONGOING)
                .notes(request.notes())
                .build();
        Trip saved = tripRepository.save(trip);
        return toResponse(saved);
    }

    @Transactional
    public TripResponse update(Long id, TripRequest request) {
        validateDates(request);
        Trip trip = getTripEntity(id);
        Vessel vessel = vesselService.findById(request.vesselId());
        trip.setVessel(vessel);
        trip.setDepartureDate(request.departureDate());
        trip.setEstimatedReturnDate(request.estimatedReturnDate());
        if (request.status() != null) {
            trip.setStatus(request.status());
        }
        trip.setNotes(request.notes());
        Trip saved = tripRepository.save(trip);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Trip trip = getTripEntity(id);
        List<TripExpense> expenses = tripExpenseRepository.findByTripIdOrderByTransactionDateDescIdDesc(id);
        for (TripExpense expense : expenses) {
            kasbonTransactionService.deleteByTripExpenseId(expense.getId());
        }
        tripExpenseRepository.deleteAll(expenses);
        tripIncomeRepository.deleteByTripId(id);
        tripRepository.delete(trip);
    }

    private void validateDates(TripRequest request) {
        if (request.estimatedReturnDate().isBefore(request.departureDate())) {
            throw new IllegalArgumentException("Estimated return date cannot be earlier than departure date");
        }
    }

    private TripResponse toResponse(Trip trip) {
        BigDecimal totalIncome = tripIncomeRepository.sumAmountByTripId(trip.getId());
        BigDecimal totalExpense = tripExpenseRepository.sumAmountByTripId(trip.getId());
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }
        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        return new TripResponse(
                trip.getId(),
                trip.getVessel(),
                trip.getDepartureDate(),
                trip.getEstimatedReturnDate(),
                trip.getStatus(),
                trip.getNotes(),
                totalIncome,
                totalExpense,
                netProfit,
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
