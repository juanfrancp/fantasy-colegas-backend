package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.ErrorResponse;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerMatchStatsResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.Match;
import com.fantasycolegas.fantasy_colegas_backend.model.PlayerMatchStats;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.LeagueService;
import com.fantasycolegas.fantasy_colegas_backend.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final LeagueService leagueService; // Necesario para la validación

    public MatchController(MatchService matchService, LeagueService leagueService) {
        this.matchService = matchService;
        this.leagueService = leagueService;
    }

    // Endpoint para crear un partido
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#matchCreateDto.leagueId, principal.id)")
    @PostMapping
    public ResponseEntity<MatchResponseDto> createMatch(
            @Valid @RequestBody MatchCreateDto matchCreateDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MatchResponseDto newMatchDto = matchService.createMatch(matchCreateDto);
        return new ResponseEntity<>(newMatchDto, HttpStatus.CREATED);
    }

    // Endpoint para actualizar las estadísticas de un jugador en un partido
    // Se valida si el usuario es admin de la liga a la que pertenece el partido
    @PreAuthorize("@matchService.checkIfUserIsAdminOfMatchLeague(#matchId, principal.id)")
    @PatchMapping("/{matchId}/stats")
    public ResponseEntity<?> updatePlayerStats(@PathVariable Long matchId,
                                               @Valid @RequestBody PlayerMatchStatsUpdateDto statsUpdateDto,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerMatchStatsResponseDto updatedStatsDto = matchService.updatePlayerStats(matchId, statsUpdateDto);
            return ResponseEntity.ok(updatedStatsDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }
}