package com.fishcatch.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.fishcatch.backend.dto.AuthResponse;
import com.fishcatch.backend.dto.CrewMemberRequest;
import com.fishcatch.backend.dto.KasbonTransactionRequest;
import com.fishcatch.backend.dto.RegisterRequest;
import com.fishcatch.backend.dto.TripExpenseRequest;
import com.fishcatch.backend.dto.TripIncomeRequest;
import com.fishcatch.backend.dto.TripRequest;
import com.fishcatch.backend.dto.TripResponse;
import com.fishcatch.backend.dto.VesselRequest;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.model.ExpenseCategory;
import com.fishcatch.backend.model.KasbonTransaction;
import com.fishcatch.backend.model.KasbonTransactionType;
import com.fishcatch.backend.model.TripExpense;
import com.fishcatch.backend.model.TripStatus;
import com.fishcatch.backend.model.Vessel;
import com.fishcatch.backend.model.VesselStatus;
import com.fishcatch.backend.security.CurrentUserContext;
import com.fishcatch.backend.service.AuthService;
import com.fishcatch.backend.service.CrewMemberService;
import com.fishcatch.backend.service.DemoWorkspaceService;
import com.fishcatch.backend.service.KasbonTransactionService;
import com.fishcatch.backend.service.TripExpenseService;
import com.fishcatch.backend.service.TripIncomeService;
import com.fishcatch.backend.service.TripService;
import com.fishcatch.backend.service.VesselService;
import com.fishcatch.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private DemoWorkspaceService demoWorkspaceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUserContext currentUserContext;

    @Autowired
    private VesselService vesselService;

    @Autowired
    private CrewMemberService crewMemberService;

    @Autowired
    private KasbonTransactionService kasbonService;

    @Autowired
    private TripService tripService;

    @Autowired
    private TripIncomeService tripIncomeService;

    @Autowired
    private TripExpenseService tripExpenseService;

    @AfterEach
    void tearDown() {
        currentUserContext.clear();
    }

    @Test
    void fullTripAndKasbonDeductionWorkflowAndUserIsolationWorksCorrectly() {
        // 0. Register User A and set active session
        AuthResponse userA = authService.register(new RegisterRequest(
                "Juragan Andi",
                "andi@fishcatch.com",
                "password123"
        ));
        currentUserContext.setCurrentUserId(userA.user().getId());

        // 1. Create vessel
        Vessel vessel = vesselService.create(new VesselRequest(
                "KM Samudra Jaya 01",
                "GT.30 No.101",
                VesselStatus.ACTIVE,
                "Main purse seine vessel"
        ));
        assertThat(vessel.getId()).isNotNull();

        // 2. Create crew member (ABK)
        CrewMember budi = crewMemberService.create(new CrewMemberRequest(
                "Budi Santoso",
                "Deckhand (ABK)",
                "08123456789",
                true
        ));
        assertThat(budi.getTotalKasbon()).isEqualByComparingTo("0");

        // 3. Crew member takes a kasbon loan of Rp 500,000
        kasbonService.create(new KasbonTransactionRequest(
                budi.getId(),
                LocalDate.of(2026, 11, 1),
                KasbonTransactionType.LOAN,
                new BigDecimal("500000"),
                "Cash advance before trip"
        ));

        CrewMember budiAfterLoan = crewMemberService.findById(budi.getId());
        assertThat(budiAfterLoan.getTotalKasbon()).isEqualByComparingTo("500000");
        assertThat(crewMemberService.findWithActiveDebt())
                .extracting(CrewMember::getId)
                .contains(budi.getId());

        // 4. Create a Trip
        TripResponse trip = tripService.create(new TripRequest(
                vessel.getId(),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 5),
                TripStatus.ONGOING,
                "November fishing trip"
        ));

        // 5. Add Trip Income (Fish Sales) of Rp 10,000,000
        tripIncomeService.create(trip.id(), new TripIncomeRequest(
                LocalDate.of(2026, 11, 5),
                "Tuna & Skipjack catch sale",
                new BigDecimal("10000000")
        ));

        // 6. Add normal Trip Expense (Fuel & Ice) of Rp 2,500,000
        tripExpenseService.create(trip.id(), new TripExpenseRequest(
                LocalDate.of(2026, 11, 2),
                ExpenseCategory.TRIP_CAPITAL,
                new BigDecimal("2500000"),
                "Diesel fuel, ice blocks, and food supplies",
                false,
                null,
                null
        ));

        // 7. Add Crew Wage Expense of Rp 2,000,000 with Rp 500,000 Kasbon Deduction
        TripExpense wageExpense = tripExpenseService.create(trip.id(), new TripExpenseRequest(
                LocalDate.of(2026, 11, 5),
                ExpenseCategory.CREW_WAGE,
                new BigDecimal("2000000"),
                "Trip wage for Budi Santoso",
                true,
                budi.getId(),
                new BigDecimal("500000")
        ));

        assertThat(wageExpense.getAmount()).isEqualByComparingTo("2000000");
        assertThat(wageExpense.getKasbonDeductionAmount()).isEqualByComparingTo("500000");
        assertThat(wageExpense.getNetPaidAmount()).isEqualByComparingTo("1500000");

        // Verify Budi's kasbon debt is now 0 and a REPAYMENT log exists
        CrewMember budiAfterDeduction = crewMemberService.findById(budi.getId());
        assertThat(budiAfterDeduction.getTotalKasbon()).isEqualByComparingTo("0");

        List<KasbonTransaction> budiTxList = kasbonService.findAll(budi.getId());
        assertThat(budiTxList).hasSize(2);
        assertThat(budiTxList).anySatisfy(tx -> {
            assertThat(tx.getType()).isEqualTo(KasbonTransactionType.REPAYMENT);
            assertThat(tx.getAmount()).isEqualByComparingTo("500000");
            assertThat(tx.getTripExpenseId()).isEqualTo(wageExpense.getId());
        });

        // Verify Trip Financial Summary uses full original expense (2,500,000 + 2,000,000 = 4,500,000)
        TripResponse summary = tripService.findById(trip.id());
        assertThat(summary.totalIncome()).isEqualByComparingTo("10000000");
        assertThat(summary.totalExpense()).isEqualByComparingTo("4500000");
        assertThat(summary.netProfit()).isEqualByComparingTo("5500000");

        // 8. Update wage expense deduction from Rp 500,000 to Rp 300,000
        TripExpense updatedWage = tripExpenseService.update(trip.id(), wageExpense.getId(), new TripExpenseRequest(
                LocalDate.of(2026, 11, 5),
                ExpenseCategory.CREW_WAGE,
                new BigDecimal("2000000"),
                "Trip wage for Budi Santoso (partial kasbon deduction)",
                true,
                budi.getId(),
                new BigDecimal("300000")
        ));
        assertThat(updatedWage.getNetPaidAmount()).isEqualByComparingTo("1700000");
        assertThat(crewMemberService.findById(budi.getId()).getTotalKasbon())
                .isEqualByComparingTo("200000");

        // 9. Delete wage expense -> Budi's kasbon debt returns to 500,000
        tripExpenseService.delete(trip.id(), wageExpense.getId());
        assertThat(crewMemberService.findById(budi.getId()).getTotalKasbon())
                .isEqualByComparingTo("500000");

        // 10. Verify Multi-Account Data Isolation: Register User B -> sees 0 vessels, 0 crew, 0 trips
        AuthResponse userB = authService.register(new RegisterRequest(
                "Juragan Hasan",
                "hasan@fishcatch.com",
                "password123"
        ));
        currentUserContext.setCurrentUserId(userB.user().getId());
        assertThat(vesselService.findAll()).isEmpty();
        assertThat(crewMemberService.findAll()).isEmpty();
        assertThat(tripService.findAll()).isEmpty();
        assertThat(kasbonService.findAll(null)).isEmpty();

        // 11. Verify Ephemeral Demo Workspace creation & complete cleanup on logout
        AuthResponse demoAuth = demoWorkspaceService.createEphemeralDemoSession();
        Long demoUserId = demoAuth.user().getId();
        assertThat(demoAuth.user().getIsDemo()).isTrue();

        currentUserContext.setCurrentUserId(demoUserId);
        assertThat(vesselService.findAll()).hasSize(2);
        assertThat(crewMemberService.findAll()).hasSize(3);
        assertThat(tripService.findAll()).hasSize(1);
        assertThat(kasbonService.findAll(null)).isNotEmpty();

        demoWorkspaceService.logoutAndCleanupIfDemo();
        assertThat(userRepository.findById(demoUserId)).isEmpty();
        assertThat(vesselService.findAll()).isEmpty();
        assertThat(crewMemberService.findAll()).isEmpty();
        assertThat(tripService.findAll()).isEmpty();
        assertThat(kasbonService.findAll(null)).isEmpty();
    }
}
