package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService; // Para reutilizar el check de admin

    public PlayerService(PlayerRepository playerRepository, LeagueRepository leagueRepository, LeagueService leagueService) {
        this.playerRepository = playerRepository;
        this.leagueRepository = leagueRepository;
        this.leagueService = leagueService;
    }

    @Transactional
    public PlayerResponseDto updatePlayer(Long leagueId, Long playerId, PlayerUpdateDto playerUpdateDto, Long userId) {
        // 1. Verificación de permisos de administrador
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para modificar jugadores en esta liga.");
        }

        // 2. Obtener el jugador por su ID
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        // 3. Verificar que el jugador pertenece a la liga correcta
        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        // 4. Actualizar los campos si se proveen en el DTO
        if (playerUpdateDto.getName() != null && !playerUpdateDto.getName().isBlank()) {
            player.setName(playerUpdateDto.getName());
        }
        if (playerUpdateDto.getImage() != null && !playerUpdateDto.getImage().isBlank()) {
            player.setImage(playerUpdateDto.getImage());
        }

        // 5. Guardar el jugador actualizado
        Player updatedPlayer = playerRepository.save(player);
        return mapToPlayerResponseDto(updatedPlayer);
    }

    @Transactional
    public PlayerResponseDto createPlayer(Long leagueId, PlayerCreateDto playerCreateDto, Long userId) {
        // 1. Verificación de permisos de administrador
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para añadir jugadores a esta liga.");
        }

        // 2. Obtener la liga
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        // 3. Crear el nuevo jugador
        Player player = new Player();
        player.setName(playerCreateDto.getName());
        player.setLeague(league);
        player.setTotalPoints(0); // <--- Campo totalPoints inicializado a 0

        // 4. Asignar imagen (por defecto si no se provee)
        if (playerCreateDto.getImage() != null && !playerCreateDto.getImage().isBlank()) {
            player.setImage(playerCreateDto.getImage());
        } else {
            player.setImage("https://example.com/default-player.jpg");
        }

        // 5. Guardar el jugador
        Player savedPlayer = playerRepository.save(player);
        return mapToPlayerResponseDto(savedPlayer);
    }

    @Transactional
    public void deletePlayer(Long leagueId, Long playerId, Long userId) {
        // 1. Verificación de permisos de administrador
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para eliminar jugadores en esta liga.");
        }

        // 2. Obtener el jugador por su ID
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        // 3. Verificar que el jugador pertenece a la liga correcta
        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        // 4. Eliminar el jugador
        playerRepository.delete(player);
    }

    public PlayerResponseDto getPlayerById(Long leagueId, Long playerId) {
        // 1. Obtener el jugador por su ID
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        // 2. Verificar que el jugador pertenece a la liga correcta
        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        // 3. Mapear y retornar el DTO
        return mapToPlayerResponseDto(player);
    }

    private PlayerResponseDto mapToPlayerResponseDto(Player player) {
        return new PlayerResponseDto(player.getId(), player.getName(), player.getImage(), player.getTotalPoints());
    }
}