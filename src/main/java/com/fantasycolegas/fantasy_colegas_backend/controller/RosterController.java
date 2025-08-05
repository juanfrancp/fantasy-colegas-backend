package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterPlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.service.RosterService;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @PostMapping("/leagues/{leagueId}/rosters")
    public ResponseEntity<String> createRoster(@PathVariable Long leagueId,
                                               @Valid @RequestBody RosterCreateDto rosterCreateDto,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            String message = rosterService.createRoster(leagueId, rosterCreateDto, currentUser.getId());
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/leagues/{leagueId}/rosters")
    public ResponseEntity<?> getUserRoster(@PathVariable Long leagueId,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            List<RosterPlayerResponseDto> roster = rosterService.getUserRoster(leagueId, currentUser.getId());
            return ResponseEntity.ok(roster);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}