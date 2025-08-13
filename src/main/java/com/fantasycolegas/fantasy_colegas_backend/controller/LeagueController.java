package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.ChangeRoleDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.JoinLeagueDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueTeamSizeUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.*;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.LeagueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Controlador REST para la gestión de ligas.
 * <p>
 * Proporciona endpoints para crear, unirse, gestionar y consultar información
 * sobre las ligas, incluyendo marcadores, rosters y solicitudes de unión.
 * </p>
 */
@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }


    /**
     * Obtiene el marcador de una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link UserScoreDto} con las puntuaciones de los usuarios.
     */
    @GetMapping("/{leagueId}/scoreboard")
    public ResponseEntity<?> getLeagueScoreboard(@PathVariable Long leagueId) {
        try {
            List<UserScoreDto> scoreboard = leagueService.getLeagueScoreboard(leagueId);
            return ResponseEntity.ok(scoreboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el marcador de la liga.");
        }
    }

    /**
     * Obtiene la puntuación de un usuario específico en una liga.
     *
     * @param leagueId El ID de la liga.
     * @param userId   El ID del usuario.
     * @return Un objeto {@link UserScoreDto} con la puntuación del usuario.
     */
    @GetMapping("/users/{userId}/points")
    public ResponseEntity<?> getUserPointsInLeague(@PathVariable Long leagueId, @PathVariable Long userId) {
        try {
            UserScoreDto userPoints = leagueService.getUserPointsInLeague(leagueId, userId);
            return ResponseEntity.ok(userPoints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener la puntuación del usuario.");
        }
    }

    /**
     * Obtiene el roster de un equipo dentro de una liga específica.
     *
     * @param leagueId    El ID de la liga.
     * @param teamId      El ID del equipo (roster).
     * @param userDetails Los detalles del usuario autenticado.
     * @return Un objeto {@link RosterResponseDto} con la información del roster.
     */
    @GetMapping("/{id}/rosters/{teamId}")
    public ResponseEntity<RosterResponseDto> getRosterByTeamId(@PathVariable("id") Long leagueId, @PathVariable("teamId") Long teamId, @AuthenticationPrincipal UserDetails userDetails) {

        String requestingUsername = userDetails.getUsername();

        try {
            RosterResponseDto rosterDTO = leagueService.getRosterByTeamId(leagueId, teamId, requestingUsername);
            return ResponseEntity.ok(rosterDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Actualiza el tamaño del equipo en una liga.
     *
     * @param leagueId          El ID de la liga.
     * @param teamSizeUpdateDto DTO con el nuevo tamaño del equipo.
     * @param currentUser       El usuario autenticado.
     * @return Un objeto {@link LeagueResponseDto} de la liga actualizada.
     */
    @PatchMapping("/{leagueId}/team-size")
    public ResponseEntity<?> updateTeamSize(@PathVariable Long leagueId, @RequestBody @Valid LeagueTeamSizeUpdateDto teamSizeUpdateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeagueResponseDto updatedLeague = leagueService.updateLeagueTeamSize(leagueId, teamSizeUpdateDto, currentUser.getId());
            return ResponseEntity.ok(updatedLeague);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Cambia el rol de un usuario en una liga.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param leagueId      El ID de la liga.
     * @param targetUserId  El ID del usuario cuyo rol se va a cambiar.
     * @param changeRoleDto DTO con el nuevo rol.
     * @param currentUser   El usuario autenticado.
     * @return Una respuesta HTTP 200 si el cambio de rol es exitoso.
     */
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @PatchMapping("/{leagueId}/participants/{targetUserId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long leagueId, @PathVariable Long targetUserId, @Valid @RequestBody ChangeRoleDto changeRoleDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.changeUserRole(leagueId, currentUser.getId(), targetUserId, changeRoleDto.getNewRole());
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Permite a un usuario unirse a una liga.
     *
     * @param joinLeagueDto DTO con el código de unión de la liga.
     * @param userDetails   El usuario autenticado.
     * @return Un objeto {@link LeagueResponseDto} de la liga a la que se unió.
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinLeague(@RequestBody JoinLeagueDto joinLeagueDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            LeagueResponseDto joinedLeague = leagueService.joinLeague(joinLeagueDto.getJoinCode(), userDetails.getId());
            return ResponseEntity.ok(joinedLeague);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Crea una nueva liga.
     *
     * @param leagueCreateDto DTO con los detalles de la nueva liga.
     * @param currentUser     El usuario autenticado, que se convierte en el administrador de la liga.
     * @return Un objeto {@link LeagueResponseDto} de la liga recién creada.
     */
    @PostMapping
    public ResponseEntity<?> createLeague(@Valid @RequestBody LeagueCreateDto leagueCreateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeagueResponseDto createdLeague = leagueService.createLeague(leagueCreateDto, currentUser.getId());
            return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    /**
     * Obtiene una liga por su ID.
     *
     * @param id          El ID de la liga.
     * @param currentUser El usuario autenticado.
     * @return Un objeto {@link LeagueResponseDto} con la información de la liga.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLeagueById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeagueResponseDto leagueDto = leagueService.getLeagueById(id, currentUser.getId());
            return ResponseEntity.ok(leagueDto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Actualiza una liga existente.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param id              El ID de la liga.
     * @param leagueCreateDto DTO con los datos actualizados de la liga.
     * @param currentUser     El usuario autenticado.
     * @return Un objeto {@link LeagueResponseDto} de la liga actualizada.
     */
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#id, principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeague(@PathVariable Long id, @Valid @RequestBody LeagueCreateDto leagueCreateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            LeagueResponseDto updatedLeague = leagueService.updateLeague(id, leagueCreateDto);
            return ResponseEntity.ok(updatedLeague);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Elimina una liga.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param id          El ID de la liga.
     * @param currentUser El usuario autenticado.
     * @return Una respuesta HTTP 204 si la liga se elimina con éxito.
     */
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#id, principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeague(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.deleteLeague(id, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Envía una solicitud para unirse a una liga privada.
     *
     * @param leagueId    El ID de la liga.
     * @param userDetails El usuario autenticado que envía la solicitud.
     * @return Una respuesta HTTP 200 si la solicitud se envía con éxito.
     */
    @PostMapping("/{leagueId}/request-join")
    public ResponseEntity<?> sendJoinRequest(@PathVariable Long leagueId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            leagueService.sendJoinRequest(leagueId, userDetails.getId());
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Obtiene todas las solicitudes de unión pendientes para una liga.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link JoinRequestResponseDto} de las solicitudes pendientes.
     */
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @GetMapping("/{leagueId}/requests")
    public ResponseEntity<List<JoinRequestResponseDto>> getPendingJoinRequests(@PathVariable Long leagueId) {
        List<LeagueJoinRequest> requests = leagueService.getPendingJoinRequests(leagueId);

        List<JoinRequestResponseDto> requestsDto = requests.stream().map(this::mapToJoinRequestDto).collect(Collectors.toList());

        return ResponseEntity.ok(requestsDto);
    }

    /**
     * Acepta una solicitud de unión a la liga.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param leagueId  El ID de la liga.
     * @param requestId El ID de la solicitud de unión.
     * @return Una respuesta HTTP 200 si la solicitud es aceptada con éxito.
     */
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

    /**
     * Rechaza una solicitud de unión a la liga.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param leagueId  El ID de la liga.
     * @param requestId El ID de la solicitud de unión.
     * @return Una respuesta HTTP 200 si la solicitud es rechazada con éxito.
     */
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

    /**
     * Permite a un usuario dejar una liga.
     *
     * @param leagueId    El ID de la liga.
     * @param currentUser El usuario autenticado.
     * @return Una respuesta HTTP 204 si el usuario abandona la liga con éxito.
     */
    @DeleteMapping("/{leagueId}/leave")
    public ResponseEntity<?> leaveLeague(@PathVariable Long leagueId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.leaveLeague(leagueId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Expulsa a un usuario de una liga.
     * <p>
     * Requiere que el usuario autenticado sea administrador de la liga.
     * </p>
     *
     * @param leagueId     El ID de la liga.
     * @param targetUserId El ID del usuario a expulsar.
     * @param currentUser  El usuario autenticado.
     * @return Una respuesta HTTP 204 si el usuario es expulsado con éxito.
     */
    @PreAuthorize("@leagueService.checkIfUserIsAdmin(#leagueId, principal.id)")
    @DeleteMapping("/{leagueId}/expel/{targetUserId}")
    public ResponseEntity<?> expelUser(@PathVariable Long leagueId, @PathVariable Long targetUserId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            leagueService.expelUser(leagueId, currentUser.getId(), targetUserId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        }
    }

    /**
     * Mapea un objeto {@link LeagueJoinRequest} a un {@link JoinRequestResponseDto}.
     *
     * @param request El objeto de solicitud de unión a la liga.
     * @return Un DTO de respuesta de la solicitud de unión.
     */
    private JoinRequestResponseDto mapToJoinRequestDto(LeagueJoinRequest request) {
        return new JoinRequestResponseDto(request.getId(), request.getUser().getId(), request.getUser().getUsername(), request.getRequestDate());
    }
}