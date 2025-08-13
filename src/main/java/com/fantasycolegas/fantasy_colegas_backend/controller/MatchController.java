package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.ErrorResponse;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerMatchStatsResponseDto;
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

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Controlador REST para la gestión de partidos.
 * <p>
 * Proporciona endpoints para crear partidos y actualizar las estadísticas de los jugadores en un partido.
 * La creación y actualización de partidos están protegidas y solo pueden ser realizadas por un administrador de la liga.
 * </p>
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final LeagueService leagueService;

    public MatchController(MatchService matchService, LeagueService leagueService) {
        this.matchService = matchService;
        this.leagueService = leagueService;
    }

    /**
     * Crea un nuevo partido.
     * <p>
     * Este endpoint está protegido y solo el administrador de la liga asociada
     * puede crear un partido.
     * </p>
     *
     * @param matchCreateDto DTO con los detalles del partido a crear.
     * @param userDetails    El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} con el {@link MatchResponseDto} del partido creado.
     */
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#matchCreateDto.leagueId, principal.id)")
    @PostMapping
    public ResponseEntity<MatchResponseDto> createMatch(@Valid @RequestBody MatchCreateDto matchCreateDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        MatchResponseDto newMatchDto = matchService.createMatch(matchCreateDto);
        return new ResponseEntity<>(newMatchDto, HttpStatus.CREATED);
    }

    /**
     * Actualiza las estadísticas de un jugador en un partido.
     * <p>
     * Este endpoint está protegido y solo un administrador de la liga del partido
     * puede actualizar las estadísticas.
     * </p>
     *
     * @param matchId        El ID del partido.
     * @param statsUpdateDto DTO con las estadísticas actualizadas del jugador.
     * @param currentUser    El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} con el {@link PlayerMatchStatsResponseDto} de las estadísticas actualizadas.
     */
    @PreAuthorize("@matchService.checkIfUserIsAdminOfMatchLeague(#matchId, principal.id)")
    @PatchMapping("/{matchId}/stats")
    public ResponseEntity<?> updatePlayerStats(@PathVariable Long matchId, @Valid @RequestBody PlayerMatchStatsUpdateDto statsUpdateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerMatchStatsResponseDto updatedStatsDto = matchService.updatePlayerStats(matchId, statsUpdateDto);
            return ResponseEntity.ok(updatedStatsDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }
}