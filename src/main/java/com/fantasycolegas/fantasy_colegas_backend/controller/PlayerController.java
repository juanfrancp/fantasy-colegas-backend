package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PointsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.ErrorResponse;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Controlador REST para la gestión de jugadores.
 * <p>
 * Proporciona endpoints para crear, actualizar, eliminar y consultar información
 * sobre jugadores en una liga específica.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Crea un nuevo jugador en una liga.
     * <p>
     * Este endpoint requiere que el usuario autenticado sea un administrador de la liga.
     * </p>
     *
     * @param leagueId        El ID de la liga donde se creará el jugador.
     * @param playerCreateDto DTO con los datos del nuevo jugador.
     * @param currentUser     El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} con el {@link PlayerResponseDto} del jugador creado.
     */
    @PostMapping("/leagues/{leagueId}/players")
    public ResponseEntity<?> createPlayer(@PathVariable Long leagueId, @Valid @RequestBody PlayerCreateDto playerCreateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerResponseDto playerDto = playerService.createPlayer(leagueId, playerCreateDto, currentUser.getId());
            return new ResponseEntity<>(playerDto, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Actualiza la información de un jugador existente.
     * <p>
     * Este endpoint requiere que el usuario autenticado sea un administrador de la liga.
     * </p>
     *
     * @param leagueId        El ID de la liga a la que pertenece el jugador.
     * @param playerId        El ID del jugador a actualizar.
     * @param playerUpdateDto DTO con los datos a actualizar del jugador.
     * @param currentUser     El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} con el {@link PlayerResponseDto} del jugador actualizado.
     */
    @PatchMapping("/leagues/{leagueId}/players/{playerId}")
    public ResponseEntity<?> updatePlayer(@PathVariable Long leagueId, @PathVariable Long playerId, @RequestBody PlayerUpdateDto playerUpdateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerResponseDto playerDto = playerService.updatePlayer(leagueId, playerId, playerUpdateDto, currentUser.getId());
            return ResponseEntity.ok(playerDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Elimina un jugador de una liga.
     * <p>
     * Este endpoint requiere que el usuario autenticado sea un administrador de la liga.
     * </p>
     *
     * @param leagueId    El ID de la liga a la que pertenece el jugador.
     * @param playerId    El ID del jugador a eliminar.
     * @param currentUser El usuario autenticado que realiza la petición.
     * @return Una {@link ResponseEntity} sin contenido si la eliminación es exitosa.
     */
    @DeleteMapping("/leagues/{leagueId}/players/{playerId}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long leagueId, @PathVariable Long playerId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            playerService.deletePlayer(leagueId, playerId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Obtiene los detalles de un jugador por su ID.
     *
     * @param leagueId El ID de la liga a la que pertenece el jugador.
     * @param playerId El ID del jugador a consultar.
     * @return Una {@link ResponseEntity} con el {@link PlayerResponseDto} del jugador.
     */
    @GetMapping("/leagues/{leagueId}/players/{playerId}")
    public ResponseEntity<?> getPlayerById(@PathVariable Long leagueId, @PathVariable Long playerId) {
        try {
            PlayerResponseDto playerDto = playerService.getPlayerById(leagueId, playerId);
            return ResponseEntity.ok(playerDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Actualiza los puntos de un jugador.
     * <p>
     * Este endpoint requiere que el usuario autenticado sea un administrador de la liga.
     * </p>
     *
     * @param leagueId        El ID de la liga.
     * @param playerId        El ID del jugador.
     * @param pointsUpdateDto DTO con los puntos a actualizar.
     * @param currentUser     El usuario autenticado.
     * @return Una {@link ResponseEntity} con el {@link PlayerResponseDto} del jugador con los puntos actualizados.
     */
    @PatchMapping("/leagues/{leagueId}/players/{playerId}/points")
    public ResponseEntity<?> updatePlayerPoints(@PathVariable Long leagueId, @PathVariable Long playerId, @RequestBody PointsUpdateDto pointsUpdateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerResponseDto playerDto = playerService.updatePlayerPoints(leagueId, playerId, pointsUpdateDto, currentUser.getId());
            return ResponseEntity.ok(playerDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}