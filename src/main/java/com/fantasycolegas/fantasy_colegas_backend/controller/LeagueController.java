package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.JoinLeagueDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
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
    public ResponseEntity<LeagueResponseDto> joinLeague(@RequestBody JoinLeagueDto joinLeagueDto,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            LeagueResponseDto joinedLeague = leagueService.joinLeague(joinLeagueDto.getJoinCode(), userDetails.getId());
            return ResponseEntity.ok(joinedLeague);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Endpoint para crear una nueva liga.
     * El usuario autenticado es automáticamente asignado como administrador y participante.
     * @param leagueCreateDto DTO con los datos de la liga a crear.
     * @param currentUser Detalles del usuario autenticado.
     * @return ResponseEntity con la liga creada y el estado 201 Created.
     */
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

    /**
     * Endpoint para obtener una liga por su ID.
     * Cualquier usuario autenticado puede ver los detalles de una liga.
     * @param id El ID de la liga.
     * @return ResponseEntity con la liga encontrada o un error 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponseDto> getLeagueById(@PathVariable Long id) {
        // La lógica de búsqueda y mapeo está ahora en el servicio
        LeagueResponseDto leagueDto = leagueService.getLeagueById(id);
        return ResponseEntity.ok(leagueDto);
    }

    /**
     * Endpoint para actualizar los datos de una liga.
     * Solo el administrador de la liga puede modificarla.
     * @param id El ID de la liga a actualizar.
     * @param leagueCreateDto DTO con los datos actualizados de la liga.
     * @param currentUser Detalles del usuario autenticado para verificar permisos.
     * @return ResponseEntity con la liga actualizada.
     */
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

    /**
     * Endpoint para eliminar una liga.
     * Solo el administrador de la liga puede eliminarla.
     * @param id El ID de la liga a eliminar.
     * @return ResponseEntity sin contenido si la operación es exitosa.
     */
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
}