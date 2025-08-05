package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerUpdateDto;
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

@RestController
@RequestMapping("/api")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/leagues/{leagueId}/players")
    public ResponseEntity<?> createPlayer(@PathVariable Long leagueId,
                                          @Valid @RequestBody PlayerCreateDto playerCreateDto,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerResponseDto playerDto = playerService.createPlayer(leagueId, playerCreateDto, currentUser.getId());
            return new ResponseEntity<>(playerDto, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PatchMapping("/leagues/{leagueId}/players/{playerId}")
    public ResponseEntity<?> updatePlayer(@PathVariable Long leagueId,
                                          @PathVariable Long playerId,
                                          @RequestBody PlayerUpdateDto playerUpdateDto,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            PlayerResponseDto playerDto = playerService.updatePlayer(leagueId, playerId, playerUpdateDto, currentUser.getId());
            return ResponseEntity.ok(playerDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @DeleteMapping("/leagues/{leagueId}/players/{playerId}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long leagueId,
                                          @PathVariable Long playerId,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            playerService.deletePlayer(leagueId, playerId, currentUser.getId());
            return ResponseEntity.noContent().build(); // <--- Respuesta 204 No Content
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/leagues/{leagueId}/players/{playerId}")
    public ResponseEntity<?> getPlayerById(@PathVariable Long leagueId,
                                           @PathVariable Long playerId) {
        try {
            PlayerResponseDto playerDto = playerService.getPlayerById(leagueId, playerId);
            return ResponseEntity.ok(playerDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}