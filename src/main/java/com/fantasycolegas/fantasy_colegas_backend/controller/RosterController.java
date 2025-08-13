package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.AddPlayerToRosterDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterPlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.RosterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Controlador REST para la gestión de rosters (equipos).
 * <p>
 * Proporciona endpoints para crear, consultar y modificar los rosters de los usuarios
 * dentro de una liga específica.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    /**
     * Crea un roster para un usuario en una liga.
     * <p>
     * Recibe los datos del roster a crear y asigna el roster al usuario autenticado en la liga especificada.
     * </p>
     *
     * @param leagueId        El ID de la liga donde se creará el roster.
     * @param rosterCreateDto DTO con el nombre del roster.
     * @param currentUser     El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} con un mensaje de éxito o un error si la creación falla.
     */
    @PostMapping("/leagues/{leagueId}/rosters")
    public ResponseEntity<String> createRoster(@PathVariable Long leagueId, @Valid @RequestBody RosterCreateDto rosterCreateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            String message = rosterService.createRoster(leagueId, rosterCreateDto, currentUser.getId());
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Obtiene el roster del usuario autenticado en una liga.
     *
     * @param leagueId    El ID de la liga.
     * @param currentUser El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} con una lista de {@link RosterPlayerResponseDto} del roster.
     */
    @GetMapping("/leagues/{leagueId}/rosters")
    public ResponseEntity<?> getUserRoster(@PathVariable Long leagueId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            List<RosterPlayerResponseDto> roster = rosterService.getUserRoster(leagueId, currentUser.getId());
            return ResponseEntity.ok(roster);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Elimina un jugador del roster del usuario autenticado.
     *
     * @param leagueId    El ID de la liga.
     * @param playerId    El ID del jugador a eliminar del roster.
     * @param currentUser El usuario autenticado.
     * @return Una {@link ResponseEntity} con un mensaje de éxito.
     */
    @DeleteMapping("/leagues/{leagueId}/rosters/players/{playerId}")
    public ResponseEntity<String> removePlayerFromRoster(@PathVariable Long leagueId, @PathVariable Long playerId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            String message = rosterService.removePlayerFromRoster(leagueId, currentUser.getId(), playerId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Añade un jugador al roster del usuario autenticado.
     *
     * @param leagueId     El ID de la liga.
     * @param addPlayerDto DTO con el ID del jugador y la posición a asignar.
     * @param currentUser  El usuario autenticado.
     * @return Una {@link ResponseEntity} con un mensaje de éxito.
     */
    @PutMapping("/leagues/{leagueId}/rosters/players")
    public ResponseEntity<String> addPlayerToRoster(@PathVariable Long leagueId, @RequestBody AddPlayerToRosterDto addPlayerDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            String message = rosterService.addPlayerToRoster(leagueId, currentUser.getId(), addPlayerDto.getPlayerId(), addPlayerDto.getPosition());
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}