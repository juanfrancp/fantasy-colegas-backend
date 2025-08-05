package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.JoinLeagueDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.ErrorResponse;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.JoinRequestResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.LeagueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    // Inyección de dependencias
    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    // Endpoint para unirse a una liga
    @PostMapping("/join")
    public ResponseEntity<?> joinLeague(@RequestBody JoinLeagueDto joinLeagueDto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            LeagueResponseDto joinedLeague = leagueService.joinLeague(joinLeagueDto.getJoinCode(), userDetails.getId());
            return ResponseEntity.ok(joinedLeague);
        } catch (ResponseStatusException e) {
            // Devolvemos el status code y el mensaje de error en el cuerpo
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PostMapping("/{leagueId}/request-join")
    public ResponseEntity<?> sendJoinRequest(@PathVariable Long leagueId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            leagueService.sendJoinRequest(leagueId, userDetails.getId());
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @GetMapping("/{leagueId}/requests")
    public ResponseEntity<List<JoinRequestResponseDto>> getPendingJoinRequests(@PathVariable Long leagueId) {
        List<LeagueJoinRequest> requests = leagueService.getPendingJoinRequests(leagueId);

        List<JoinRequestResponseDto> requestsDto = requests.stream()
                .map(this::mapToJoinRequestDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(requestsDto);
    }

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @PostMapping("/{leagueId}/requests/{requestId}/accept")
    public ResponseEntity<?> acceptJoinRequest(@PathVariable Long leagueId, @PathVariable Long requestId) {
        try {
            leagueService.acceptJoinRequest(requestId);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @PostMapping("/{leagueId}/requests/{requestId}/reject")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Long leagueId, @PathVariable Long requestId) {
        try {
            leagueService.rejectJoinRequest(requestId);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PostMapping
    public ResponseEntity<LeagueResponseDto> createLeague(@Valid @RequestBody LeagueCreateDto leagueCreateDto,
                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        League newLeague = new League();
        newLeague.setName(leagueCreateDto.getName());
        newLeague.setDescription(leagueCreateDto.getDescription());
        newLeague.setImage(leagueCreateDto.getImage());
        newLeague.setPrivate(leagueCreateDto.isPrivate());

        // Generar un joinCode de 4 dígitos (puedes mover esto al servicio si quieres)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder joinCodeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            joinCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        newLeague.setJoinCode(joinCodeBuilder.toString());

        // El servicio se encargará de añadir al usuario creador como admin y participante
        LeagueResponseDto createdLeague = leagueService.createLeague(newLeague, currentUser.getId());

        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeagueById(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeagueResponseDto leagueDto = leagueService.getLeagueById(id, currentUser.getId());
            return ResponseEntity.ok(leagueDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeagueResponseDto> updateLeague(@PathVariable Long id,
                                                          @Valid @RequestBody LeagueCreateDto leagueCreateDto,
                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Delegamos la lógica de autorización al servicio
        if (!leagueService.checkIfUserIsAdmin(id, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para esta liga.");
        }

        LeagueResponseDto updatedLeague = leagueService.updateLeague(id, leagueCreateDto); // Asumiendo que existe este método en el servicio
        return ResponseEntity.ok(updatedLeague);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeague(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Delegamos la lógica de autorización al servicio
        if (!leagueService.checkIfUserIsAdmin(id, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para esta liga.");
        }

        leagueService.deleteLeague(id); // Asumiendo que existe este método en el servicio
        return ResponseEntity.noContent().build();
    }

    private JoinRequestResponseDto mapToJoinRequestDto(LeagueJoinRequest request) {
        return new JoinRequestResponseDto(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getUsername(),
                request.getRequestDate()
        );
    }
}