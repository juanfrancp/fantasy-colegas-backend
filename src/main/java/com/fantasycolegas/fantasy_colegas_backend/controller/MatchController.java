package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResponseDto> createMatch(@Valid @RequestBody MatchCreateDto matchCreateDto) {
        MatchResponseDto createdMatch = matchService.createMatch(matchCreateDto);
        return new ResponseEntity<>(createdMatch, HttpStatus.CREATED);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MatchResponseDto>> getUpcomingMatches() {
        List<MatchResponseDto> upcomingMatches = matchService.getUpcomingMatches();
        return ResponseEntity.ok(upcomingMatches);
    }

    @GetMapping("/past")
    public ResponseEntity<List<MatchResponseDto>> getPastMatches() {
        List<MatchResponseDto> pastMatches = matchService.getPastMatches();
        return ResponseEntity.ok(pastMatches);
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<MatchResponseDto> updateMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchUpdateDto updateDto) {
        MatchResponseDto updatedMatch = matchService.updateMatch(matchId, updateDto);
        return ResponseEntity.ok(updatedMatch);
    }
}