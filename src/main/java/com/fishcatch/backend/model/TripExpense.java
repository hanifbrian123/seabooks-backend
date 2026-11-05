package com.fishcatch.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "has_kasbon_deduction", nullable = false)
    @Builder.Default
    private Boolean hasKasbonDeduction = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "crew_member_id")
    private CrewMember crewMember;

    @Column(name = "kasbon_deduction_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal kasbonDeductionAmount = BigDecimal.ZERO;

    @Column(name = "net_paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPaidAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
        if (this.hasKasbonDeduction == null) {
            this.hasKasbonDeduction = false;
        }
        if (this.kasbonDeductionAmount == null) {
            this.kasbonDeductionAmount = BigDecimal.ZERO;
        }
        if (this.netPaidAmount == null && this.amount != null) {
            this.netPaidAmount = this.amount.subtract(this.kasbonDeductionAmount);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

