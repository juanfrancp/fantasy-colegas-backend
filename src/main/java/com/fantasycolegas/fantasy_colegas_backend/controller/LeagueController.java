// Archivo: LeagueController.java
package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.ChangeRoleDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.JoinLeagueDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.ErrorResponse;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.JoinRequestResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.LeagueService;
import jakarta.validation.Valid;
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

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @PatchMapping("/{leagueId}/participants/{targetUserId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long leagueId,
                                            @PathVariable Long targetUserId,
                                            @Valid @RequestBody ChangeRoleDto changeRoleDto,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.changeUserRole(leagueId, currentUser.getId(), targetUserId, changeRoleDto.getNewRole());
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinLeague(@RequestBody JoinLeagueDto joinLeagueDto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            LeagueResponseDto joinedLeague = leagueService.joinLeague(joinLeagueDto.getJoinCode(), userDetails.getId());
            return ResponseEntity.ok(joinedLeague);
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

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder joinCodeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            joinCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        newLeague.setJoinCode(joinCodeBuilder.toString());

        // Añado aquí el numberOfPlayers para que lo tenga la liga desde un principio
        newLeague.setNumberOfPlayers(leagueCreateDto.getNumberOfPlayers());

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

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#id, principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeague(@PathVariable Long id,
                                          @Valid @RequestBody LeagueCreateDto leagueCreateDto,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeagueResponseDto updatedLeague = leagueService.updateLeague(id, leagueCreateDto);
            return ResponseEntity.ok(updatedLeague);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#id, principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeague(@PathVariable Long id) {
        try {
            leagueService.deleteLeague(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
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

    @DeleteMapping("/{leagueId}/leave")
    public ResponseEntity<?> leaveLeague(@PathVariable Long leagueId,
                                         @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.leaveLeague(leagueId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @DeleteMapping("/{leagueId}/expel/{targetUserId}")
    public ResponseEntity<?> expelUser(@PathVariable Long leagueId, @PathVariable Long targetUserId,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.expelUser(leagueId, currentUser.getId(), targetUserId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
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