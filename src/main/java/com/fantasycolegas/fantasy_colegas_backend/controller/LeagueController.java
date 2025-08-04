package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
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

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private UserRepository userRepository;


    // Helper method para mapear la entidad a DTO
    private LeagueResponseDto mapToDto(League league) {
        UserResponseDto adminDto = new UserResponseDto(league.getAdmin().getId(), league.getAdmin().getUsername());
        List<UserResponseDto> participantsDto = league.getParticipants().stream()
                .map(p -> new UserResponseDto(p.getId(), p.getUsername()))
                .collect(Collectors.toList());

        return new LeagueResponseDto(
                league.getId(),
                league.getName(),
                league.getDescription(),
                league.getImage(),
                league.isPrivate(),
                league.getJoinCode(),
                league.getNumberOfPlayers(),
                adminDto,
                participantsDto
        );
    }


    /**
     * Endpoint para crear una nueva liga.
     * @param leagueCreateDto DTO con los datos de la liga a crear.
     * @param currentUser Detalles del usuario autenticado, que será el administrador de la liga.
     * @return ResponseEntity con la liga creada y el estado 201 Created.
     */
    @PostMapping
    public ResponseEntity<LeagueResponseDto> createLeague(@Valid @RequestBody LeagueCreateDto leagueCreateDto,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Encontrar al usuario que está creando la liga
        User admin = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        League newLeague = new League();
        newLeague.setName(leagueCreateDto.getName());
        newLeague.setDescription(leagueCreateDto.getDescription());
        newLeague.setImage(leagueCreateDto.getImage());
        newLeague.setPrivate(leagueCreateDto.isPrivate());
        newLeague.setNumberOfPlayers(leagueCreateDto.getNumberOfPlayers());
        newLeague.setAdmin(admin);

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder joinCodeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            joinCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        newLeague.setJoinCode(joinCodeBuilder.toString());

        // Guardar la nueva liga en la base de datos
        League savedLeague = leagueRepository.save(newLeague);

        // Devolver el DTO de respuesta
        return new ResponseEntity<>(mapToDto(savedLeague), HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener una liga por su ID.
     * Cualquier usuario autenticado puede ver los detalles de una liga.
     * @param id El ID de la liga.
     * @return ResponseEntity con la liga encontrada o un error 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponseDto> getLeagueById(@PathVariable Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));
        return ResponseEntity.ok(mapToDto(league));
    }

    /**
     * Endpoint para actualizar los datos de una liga.
     * Solo el administrador de la liga puede modificarla.
     * @param id El ID de la liga a actualizar.
     * @param leagueCreateDto DTO con los datos actualizados de la liga.
     * @param currentUser Detalles del usuario autenticado para verificar permisos.
     * @return ResponseEntity con la liga actualizada.
     */
    @PreAuthorize("isAuthenticated() and @leagueRepository.findById(#id).get().admin.id == principal.id")
    @PutMapping("/{id}")
    public ResponseEntity<LeagueResponseDto> updateLeague(@PathVariable Long id,
                                               @Valid @RequestBody LeagueCreateDto leagueCreateDto,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        // 1. Encontrar la liga a actualizar
        League existingLeague = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        // 2. Actualizar los campos
        existingLeague.setName(leagueCreateDto.getName());
        existingLeague.setDescription(leagueCreateDto.getDescription());
        existingLeague.setImage(leagueCreateDto.getImage());
        existingLeague.setPrivate(leagueCreateDto.isPrivate());
        existingLeague.setNumberOfPlayers(leagueCreateDto.getNumberOfPlayers());

        // 3. Guardar y devolver la liga actualizada
        League updatedLeague = leagueRepository.save(existingLeague);
        return ResponseEntity.ok(mapToDto(updatedLeague));
    }

    /**
     * Endpoint para eliminar una liga.
     * Solo el administrador de la liga puede eliminarla.
     * @param id El ID de la liga a eliminar.
     * @return ResponseEntity sin contenido si la operación es exitosa.
     */
    @PreAuthorize("isAuthenticated() and @leagueRepository.findById(#id).get().admin.id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeague(@PathVariable Long id) {
        // 1. Verificar si la liga existe antes de eliminarla
        if (!leagueRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada");
        }

        // 2. Eliminar la liga
        leagueRepository.deleteById(id);

        // 3. Devolver una respuesta sin contenido
        return ResponseEntity.noContent().build();
    }
}