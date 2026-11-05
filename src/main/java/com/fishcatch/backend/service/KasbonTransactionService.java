package com.fishcatch.backend.service;

import com.fishcatch.backend.dto.KasbonTransactionRequest;
import com.fishcatch.backend.exception.ResourceNotFoundException;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.model.KasbonTransaction;
import com.fishcatch.backend.model.KasbonTransactionType;
import com.fishcatch.backend.repository.CrewMemberRepository;
import com.fishcatch.backend.repository.KasbonTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KasbonTransactionService {

    private final KasbonTransactionRepository kasbonRepository;
    private final CrewMemberRepository crewMemberRepository;

    @Transactional(readOnly = true)
    public List<KasbonTransaction> findAll(Long crewMemberId) {
        if (crewMemberId != null) {
            return kasbonRepository.findByCrewMemberIdOrderByTransactionDateDescIdDesc(crewMemberId);
        }
        return kasbonRepository.findAllByOrderByTransactionDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public KasbonTransaction findById(Long id) {
        return kasbonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kasbon transaction not found with id: " + id));
    }

    @Transactional
    public KasbonTransaction create(KasbonTransactionRequest request) {
        CrewMember crew = getCrewMember(request.crewMemberId());
        applyBalanceChange(crew, request.type(), request.amount());
        crewMemberRepository.save(crew);

        KasbonTransaction tx = KasbonTransaction.builder()
                .crewMember(crew)
                .transactionDate(request.transactionDate())
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .build();
        return kasbonRepository.save(tx);
    }

    @Transactional
    public KasbonTransaction update(Long id, KasbonTransactionRequest request) {
        KasbonTransaction existing = findById(id);
        if (existing.getTripExpenseId() != null) {
            throw new IllegalStateException(
                    "This repayment is linked to a Trip Expense. Please edit it from the Trip Expenses tab."
            );
        }

        CrewMember oldCrew = existing.getCrewMember();
        reverseBalanceChange(oldCrew, existing.getType(), existing.getAmount());
        crewMemberRepository.save(oldCrew);

        CrewMember newCrew = oldCrew.getId().equals(request.crewMemberId())
                ? oldCrew
                : getCrewMember(request.crewMemberId());

        applyBalanceChange(newCrew, request.type(), request.amount());
        crewMemberRepository.save(newCrew);

        existing.setCrewMember(newCrew);
        existing.setTransactionDate(request.transactionDate());
        existing.setType(request.type());
        existing.setAmount(request.amount());
        existing.setDescription(request.description());

        return kasbonRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        KasbonTransaction existing = findById(id);
        if (existing.getTripExpenseId() != null) {
            throw new IllegalStateException(
                    "This repayment is linked to a Trip Expense. Please delete or update the corresponding Trip Expense."
            );
        }

        CrewMember crew = existing.getCrewMember();
        reverseBalanceChange(crew, existing.getType(), existing.getAmount());
        crewMemberRepository.save(crew);

        kasbonRepository.delete(existing);
    }

    @Transactional
    public KasbonTransaction syncFromTripExpense(
            Long tripExpenseId,
            Long crewMemberId,
            LocalDate transactionDate,
            BigDecimal deductionAmount,
            String description
    ) {
        Optional<KasbonTransaction> existingOpt = kasbonRepository.findByTripExpenseId(tripExpenseId);
        if (existingOpt.isPresent()) {
            KasbonTransaction existing = existingOpt.get();
            CrewMember oldCrew = existing.getCrewMember();
            reverseBalanceChange(oldCrew, existing.getType(), existing.getAmount());
            crewMemberRepository.save(oldCrew);

            CrewMember targetCrew = oldCrew.getId().equals(crewMemberId)
                    ? oldCrew
                    : getCrewMember(crewMemberId);
            applyBalanceChange(targetCrew, KasbonTransactionType.REPAYMENT, deductionAmount);
            crewMemberRepository.save(targetCrew);

            existing.setCrewMember(targetCrew);
            existing.setTransactionDate(transactionDate);
            existing.setType(KasbonTransactionType.REPAYMENT);
            existing.setAmount(deductionAmount);
            existing.setDescription(description);
            return kasbonRepository.save(existing);
        } else {
            CrewMember targetCrew = getCrewMember(crewMemberId);
            applyBalanceChange(targetCrew, KasbonTransactionType.REPAYMENT, deductionAmount);
            crewMemberRepository.save(targetCrew);

            KasbonTransaction created = KasbonTransaction.builder()
                    .crewMember(targetCrew)
                    .tripExpenseId(tripExpenseId)
                    .transactionDate(transactionDate)
                    .type(KasbonTransactionType.REPAYMENT)
                    .amount(deductionAmount)
                    .description(description)
                    .build();
            return kasbonRepository.save(created);
        }
    }

    @Transactional
    public void deleteByTripExpenseId(Long tripExpenseId) {
        kasbonRepository.findByTripExpenseId(tripExpenseId).ifPresent(existing -> {
            CrewMember crew = existing.getCrewMember();
            reverseBalanceChange(crew, existing.getType(), existing.getAmount());
            crewMemberRepository.save(crew);
            kasbonRepository.delete(existing);
        });
    }

    private CrewMember getCrewMember(Long crewMemberId) {
        return crewMemberRepository.findById(crewMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member not found with id: " + crewMemberId));
    }

    private void applyBalanceChange(CrewMember crew, KasbonTransactionType type, BigDecimal amount) {
        BigDecimal current = crew.getTotalKasbon() != null ? crew.getTotalKasbon() : BigDecimal.ZERO;
        if (type == KasbonTransactionType.LOAN) {
            crew.setTotalKasbon(current.add(amount));
        } else {
            if (current.compareTo(amount) < 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "Repayment amount (%s) exceeds %s's remaining kasbon balance (%s)",
                                amount.toPlainString(),
                                crew.getName(),
                                current.toPlainString()
                        )
                );
            }
            crew.setTotalKasbon(current.subtract(amount));
        }
    }

    private void reverseBalanceChange(CrewMember crew, KasbonTransactionType type, BigDecimal amount) {
        BigDecimal current = crew.getTotalKasbon() != null ? crew.getTotalKasbon() : BigDecimal.ZERO;
        if (type == KasbonTransactionType.LOAN) {
            BigDecimal updated = current.subtract(amount);
            if (updated.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Cannot remove or reduce this loan because part of it has already been repaid."
                );
            }
            crew.setTotalKasbon(updated);
        } else {
            crew.setTotalKasbon(current.add(amount));
        }
    }
}
