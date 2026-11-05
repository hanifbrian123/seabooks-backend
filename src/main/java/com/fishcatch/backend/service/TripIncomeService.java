package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.TripIncomeRequest;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.Trip;
import com.fishcatch.backend.model.TripIncome;
import com.fishcatch.backend.repository.TripIncomeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripIncomeService {

    private final TripIncomeRepository tripIncomeRepository;
    private final TripService tripService;

    @Transactional(readOnly = true)
    public List<TripIncome> findByTripId(Long tripId) {
        tripService.getTripEntity(tripId);
        return tripIncomeRepository.findByTripIdOrderByTransactionDateDescIdDesc(tripId);
    }

    @Transactional
    public TripIncome create(Long tripId, TripIncomeRequest request) {
        Trip trip = tripService.getTripEntity(tripId);
        TripIncome income = TripIncome.builder()
                .trip(trip)
                .transactionDate(request.transactionDate())
                .description(request.description().trim())
                .amount(request.amount())
                .build();
        return tripIncomeRepository.save(income);
    }

    @Transactional
    public TripIncome update(Long tripId, Long incomeId, TripIncomeRequest request) {
        TripIncome existing = getIncomeForTrip(tripId, incomeId);
        existing.setTransactionDate(request.transactionDate());
        existing.setDescription(request.description().trim());
        existing.setAmount(request.amount());
        return tripIncomeRepository.save(existing);
    }

    @Transactional
    public void delete(Long tripId, Long incomeId) {
        TripIncome existing = getIncomeForTrip(tripId, incomeId);
        tripIncomeRepository.delete(existing);
    }

    private TripIncome getIncomeForTrip(Long tripId, Long incomeId) {
        TripIncome income = tripIncomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip income not found with id: " + incomeId));
        if (!income.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Income record does not belong to trip id: " + tripId);
        }
        return income;
    }
}
