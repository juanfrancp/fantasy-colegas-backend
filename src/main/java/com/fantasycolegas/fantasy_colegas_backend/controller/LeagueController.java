package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint para crear una nueva liga.
     * @param leagueCreateDto DTO con los datos de la liga a crear.
     * @param currentUser Detalles del usuario autenticado, que será el administrador de la liga.
     * @return ResponseEntity con la liga creada y el estado 201 Created.
     */
    @PostMapping
    public ResponseEntity<League> createLeague(@Valid @RequestBody LeagueCreateDto leagueCreateDto,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Encontrar al usuario que está creando la liga
        User admin = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Crear una nueva instancia de League
        League newLeague = new League();
        newLeague.setName(leagueCreateDto.getName());
        newLeague.setDescription(leagueCreateDto.getDescription());
        newLeague.setImage(leagueCreateDto.getImage());
        newLeague.setPrivate(leagueCreateDto.isPrivate());
        newLeague.setNumberOfPlayers(leagueCreateDto.getNumberOfPlayers());
        newLeague.setAdmin(admin); // El usuario autenticado es el administrador

        // Generar un código de unión de 4 dígitos (puedes ajustar la lógica si lo deseas)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder joinCodeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            joinCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        newLeague.setJoinCode(joinCodeBuilder.toString());

        // Guardar la nueva liga en la base de datos
        League savedLeague = leagueRepository.save(newLeague);
        return new ResponseEntity<>(savedLeague, HttpStatus.CREATED);
    }
}