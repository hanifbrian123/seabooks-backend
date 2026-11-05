package com.fishcatch.backend.controller;

import com.fishcatch.backend.dto.KasbonTransactionRequest;
import com.fishcatch.backend.model.KasbonTransaction;
import com.fishcatch.backend.service.KasbonTransactionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kasbon")
@RequiredArgsConstructor
public class KasbonTransactionController {

    private final KasbonTransactionService kasbonService;

    @GetMapping
    public List<KasbonTransaction> getAllTransactions(
            @RequestParam(required = false) Long crewMemberId
    ) {
        return kasbonService.findAll(crewMemberId);
    }

    @GetMapping("/{id}")
    public KasbonTransaction getTransactionById(@PathVariable Long id) {
        return kasbonService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KasbonTransaction createTransaction(@Valid @RequestBody KasbonTransactionRequest request) {
        return kasbonService.create(request);
    }

    @PutMapping("/{id}")
    public KasbonTransaction updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody KasbonTransactionRequest request
    ) {
        return kasbonService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable Long id) {
        kasbonService.delete(id);
    }
}

