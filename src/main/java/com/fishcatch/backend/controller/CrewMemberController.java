package com.fishcatch.backend.controller;

import com.fishcatch.backend.dto.CrewMemberRequest;
import com.fishcatch.backend.model.CrewMember;
import com.fishcatch.backend.service.CrewMemberService;
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
@RequestMapping("/api/crew")
@RequiredArgsConstructor
public class CrewMemberController {

    private final CrewMemberService crewMemberService;

    @GetMapping
    public List<CrewMember> getAllCrewMembers() {
        return crewMemberService.findAll();
    }

    @GetMapping("/with-debt")
    public List<CrewMember> getCrewMembersWithDebt() {
        return crewMemberService.findWithActiveDebt();
    }

    @GetMapping("/{id}")
    public CrewMember getCrewMemberById(@PathVariable Long id) {
        return crewMemberService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrewMember createCrewMember(@Valid @RequestBody CrewMemberRequest request) {
        return crewMemberService.create(request);
    }

    @PutMapping("/{id}")
    public CrewMember updateCrewMember(@PathVariable Long id, @Valid @RequestBody CrewMemberRequest request) {
        return crewMemberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCrewMember(@PathVariable Long id) {
        crewMemberService.delete(id);
    }
}

