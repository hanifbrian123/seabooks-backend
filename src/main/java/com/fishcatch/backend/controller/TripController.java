package com.fishcatch.backend.controller;

import com.fishcatch.backend.dto.TripExpenseRequest;
import com.fishcatch.backend.dto.TripIncomeRequest;
import com.fishcatch.backend.dto.TripRequest;
import com.fishcatch.backend.dto.TripResponse;
import com.fishcatch.backend.model.TripExpense;
import com.fishcatch.backend.model.TripIncome;
import com.fishcatch.backend.service.TripExpenseService;
import com.fishcatch.backend.service.TripIncomeService;
import com.fishcatch.backend.service.TripService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TripIncomeService tripIncomeService;
    private final TripExpenseService tripExpenseService;

    @GetMapping
    public List<TripResponse> getAllTrips() {
        return tripService.findAll();
    }

    @GetMapping("/{id}")
    public TripResponse getTripById(@PathVariable Long id) {
        return tripService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@Valid @RequestBody TripRequest request) {
        return tripService.create(request);
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(@PathVariable Long id, @Valid @RequestBody TripRequest request) {
        return tripService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable Long id) {
        tripService.delete(id);
    }

    @GetMapping("/{tripId}/incomes")
    public List<TripIncome> getTripIncomes(@PathVariable Long tripId) {
        return tripIncomeService.findByTripId(tripId);
    }

    @PostMapping("/{tripId}/incomes")
    @ResponseStatus(HttpStatus.CREATED)
    public TripIncome createTripIncome(
            @PathVariable Long tripId,
            @Valid @RequestBody TripIncomeRequest request
    ) {
        return tripIncomeService.create(tripId, request);
    }

    @PutMapping("/{tripId}/incomes/{incomeId}")
    public TripIncome updateTripIncome(
            @PathVariable Long tripId,
            @PathVariable Long incomeId,
            @Valid @RequestBody TripIncomeRequest request
    ) {
        return tripIncomeService.update(tripId, incomeId, request);
    }

    @DeleteMapping("/{tripId}/incomes/{incomeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTripIncome(@PathVariable Long tripId, @PathVariable Long incomeId) {
        tripIncomeService.delete(tripId, incomeId);
    }

    @GetMapping("/{tripId}/expenses")
    public List<TripExpense> getTripExpenses(@PathVariable Long tripId) {
        return tripExpenseService.findByTripId(tripId);
    }

    @PostMapping("/{tripId}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public TripExpense createTripExpense(
            @PathVariable Long tripId,
            @Valid @RequestBody TripExpenseRequest request
    ) {
        return tripExpenseService.create(tripId, request);
    }

    @PutMapping("/{tripId}/expenses/{expenseId}")
    public TripExpense updateTripExpense(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @Valid @RequestBody TripExpenseRequest request
    ) {
        return tripExpenseService.update(tripId, expenseId, request);
    }

    @DeleteMapping("/{tripId}/expenses/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTripExpense(@PathVariable Long tripId, @PathVariable Long expenseId) {
        tripExpenseService.delete(tripId, expenseId);
    }
}

