package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.AuthResponse;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.model.ExpenseCategory;
import com.fishcatch.backend.model.KasbonTransaction;
import com.fishcatch.backend.model.KasbonTransactionType;
import com.fishcatch.backend.model.Trip;
import com.fishcatch.backend.model.TripExpense;
import com.fishcatch.backend.model.TripIncome;
import com.fishcatch.backend.model.TripStatus;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.model.Vessel;
import com.fishcatch.backend.model.VesselStatus;
import com.fishcatch.backend.repository.CrewMemberRepository;
import com.fishcatch.backend.repository.KasbonTransactionRepository;
import com.fishcatch.backend.repository.TripExpenseRepository;
import com.fishcatch.backend.repository.TripIncomeRepository;
import com.fishcatch.backend.repository.TripRepository;
import com.fishcatch.backend.repository.UserRepository;
import com.fishcatch.backend.repository.VesselRepository;
import com.fishcatch.backend.security.CurrentUserContext;
import com.fishcatch.backend.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoWorkspaceService {

    private static final long DEMO_SESSION_SECONDS = 2 * 3600L; // 2 hours

    private final UserRepository userRepository;
    private final VesselRepository vesselRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final KasbonTransactionRepository kasbonRepository;
    private final TripRepository tripRepository;
    private final TripIncomeRepository tripIncomeRepository;
    private final TripExpenseRepository tripExpenseRepository;
    private final JwtService jwtService;
    private final CurrentUserContext currentUserContext;

    @Transactional
    public AuthResponse createEphemeralDemoSession() {
        cleanupExpiredDemoAccounts();

        // Also clean up legacy static demo@fishcatch.com if present
        userRepository.findByEmailIgnoreCase("demo@fishcatch.com")
                .ifPresent(this::deleteUserAndAllData);

        String shortId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        User demoUser = User.builder()
                .name("Demo Visitor #" + shortId)
                .email("demo." + shortId.toLowerCase() + "@temporary.fishcatch")
                .isDemo(true)
                .demoExpiresAt(LocalDateTime.now().plusSeconds(DEMO_SESSION_SECONDS))
                .build();
        User savedUser = userRepository.save(demoUser);

        seedSampleDataForDemoUser(savedUser);

        String token = jwtService.generateToken(savedUser, DEMO_SESSION_SECONDS);
        return new AuthResponse(token, savedUser);
    }

    @Transactional
    public void logoutAndCleanupIfDemo() {
        User currentUser = currentUserContext.getCurrentUser();
        if (Boolean.TRUE.equals(currentUser.getIsDemo())) {
            deleteUserAndAllData(currentUser);
        }
    }

    @Scheduled(fixedDelay = 900_000) // Every 15 minutes
    @Transactional
    public void cleanupExpiredDemoAccounts() {
        List<User> expiredDemos = userRepository.findByIsDemoTrueAndDemoExpiresAtBefore(LocalDateTime.now());
        for (User expired : expiredDemos) {
            deleteUserAndAllData(expired);
        }
    }

    @Transactional
    public void deleteUserAndAllData(User user) {
        Long userId = user.getId();

        List<Trip> trips = tripRepository.findByUserIdOrderByDepartureDateDescIdDesc(userId);
        for (Trip trip : trips) {
            List<TripExpense> expenses = tripExpenseRepository.findByTripIdOrderByTransactionDateDescIdDesc(trip.getId());
            tripExpenseRepository.deleteAll(expenses);
            tripIncomeRepository.deleteByTripId(trip.getId());
        }
        tripRepository.deleteAll(trips);

        List<KasbonTransaction> kasbonTxs = kasbonRepository.findByUserIdOrderByTransactionDateDescIdDesc(userId);
        kasbonRepository.deleteAll(kasbonTxs);

        List<CrewMember> crewMembers = crewMemberRepository.findByUserIdOrderByNameAsc(userId);
        crewMemberRepository.deleteAll(crewMembers);

        List<Vessel> vessels = vesselRepository.findByUserIdOrderByNameAsc(userId);
        vesselRepository.deleteAll(vessels);

        userRepository.delete(user);
    }

    private void seedSampleDataForDemoUser(User demoUser) {
        // 1. Seed Vessels
        Vessel v1 = vesselRepository.save(Vessel.builder()
                .user(demoUser)
                .name("KM Samudra Jaya 01")
                .registrationNumber("GT. 35 No. 118/Pa")
                .status(VesselStatus.SAILING)
                .notes("Main purse seine vessel — Java & Banda Sea route")
                .build());

        vesselRepository.save(Vessel.builder()
                .user(demoUser)
                .name("KM Bahari Nusantara 02")
                .registrationNumber("GT. 28 No. 204/Pa")
                .status(VesselStatus.ACTIVE)
                .notes("Longline tuna vessel — Cold storage 15 tons")
                .build());

        // 2. Seed Crew Members (ABK)
        crewMemberRepository.save(CrewMember.builder()
                .user(demoUser)
                .name("Slamet Riyadi")
                .role("Captain (Nakhoda)")
                .phone("0812-3400-1101")
                .totalKasbon(BigDecimal.ZERO)
                .active(true)
                .build());

        CrewMember agus = crewMemberRepository.save(CrewMember.builder()
                .user(demoUser)
                .name("Agus Saputra")
                .role("Chief Engineer (KKM)")
                .phone("0813-5500-2202")
                .totalKasbon(new BigDecimal("1200000"))
                .active(true)
                .build());

        CrewMember budi = crewMemberRepository.save(CrewMember.builder()
                .user(demoUser)
                .name("Budi Santoso")
                .role("Deckhand (ABK)")
                .phone("0857-8800-3303")
                .totalKasbon(BigDecimal.ZERO) // 750,000 loan minus 750,000 wage deduction = 0
                .active(true)
                .build());

        // 3. Seed Kasbon Loans
        kasbonRepository.save(KasbonTransaction.builder()
                .user(demoUser)
                .crewMember(budi)
                .transactionDate(LocalDate.of(2026, 11, 1))
                .type(KasbonTransactionType.LOAN)
                .amount(new BigDecimal("750000"))
                .description("Family cash advance before departure")
                .build());

        kasbonRepository.save(KasbonTransaction.builder()
                .user(demoUser)
                .crewMember(agus)
                .transactionDate(LocalDate.of(2026, 11, 1))
                .type(KasbonTransactionType.LOAN)
                .amount(new BigDecimal("1200000"))
                .description("Cash advance before departure")
                .build());

        // 4. Seed Trip
        Trip trip = tripRepository.save(Trip.builder()
                .user(demoUser)
                .vessel(v1)
                .departureDate(LocalDate.of(2026, 11, 1))
                .estimatedReturnDate(LocalDate.of(2026, 11, 5))
                .status(TripStatus.ONGOING)
                .notes("5-day pelagic fishing trip — Pekalongan Port")
                .build());

        // 5. Seed Trip Incomes (Fish Sales)
        tripIncomeRepository.save(TripIncome.builder()
                .trip(trip)
                .transactionDate(LocalDate.of(2026, 11, 5))
                .description("Yellowfin Tuna catch sale (1,400 kg @ Rp 35,000)")
                .amount(new BigDecimal("49000000"))
                .build());

        tripIncomeRepository.save(TripIncome.builder()
                .trip(trip)
                .transactionDate(LocalDate.of(2026, 11, 5))
                .description("Skipjack & Mackerel auction sale (1,200 kg)")
                .amount(new BigDecimal("21000000"))
                .build());

        // 6. Seed Trip Expenses (1 Capital + 1 Wage with Kasbon Deduction)
        tripExpenseRepository.save(TripExpense.builder()
                .trip(trip)
                .transactionDate(LocalDate.of(2026, 11, 1))
                .category(ExpenseCategory.TRIP_CAPITAL)
                .amount(new BigDecimal("18500000"))
                .description("Solar diesel fuel (1,500L), ice blocks (80 bars), and crew food")
                .hasKasbonDeduction(false)
                .kasbonDeductionAmount(BigDecimal.ZERO)
                .netPaidAmount(new BigDecimal("18500000"))
                .build());

        TripExpense wageExpense = tripExpenseRepository.save(TripExpense.builder()
                .trip(trip)
                .transactionDate(LocalDate.of(2026, 11, 5))
                .category(ExpenseCategory.CREW_WAGE)
                .amount(new BigDecimal("3000000"))
                .description("Trip wage for Budi Santoso (Deckhand)")
                .hasKasbonDeduction(true)
                .crewMember(budi)
                .kasbonDeductionAmount(new BigDecimal("750000"))
                .netPaidAmount(new BigDecimal("2250000"))
                .build());

        // Linked Kasbon Repayment from Trip Wage Deduction
        kasbonRepository.save(KasbonTransaction.builder()
                .user(demoUser)
                .crewMember(budi)
                .tripExpenseId(wageExpense.getId())
                .transactionDate(LocalDate.of(2026, 11, 5))
                .type(KasbonTransactionType.REPAYMENT)
                .amount(new BigDecimal("750000"))
                .description("Wage deduction on Trip #" + trip.getId() + " (KM Samudra Jaya 01) — Trip wage for Budi Santoso (Deckhand)")
                .build());
    }
}
