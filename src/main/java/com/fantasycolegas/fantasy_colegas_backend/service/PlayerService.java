package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PointsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.model.RosterPlayer;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.RosterPlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;
    private final RosterPlayerRepository rosterPlayerRepository;

    public PlayerService(PlayerRepository playerRepository, LeagueRepository leagueRepository, LeagueService leagueService, RosterPlayerRepository rosterPlayerRepository) {
        this.playerRepository = playerRepository;
        this.leagueRepository = leagueRepository;
        this.leagueService = leagueService;
        this.rosterPlayerRepository = rosterPlayerRepository;
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

        // 4. Obtener el jugador vacío (placeholder)
        Player placeholderPlayer = playerRepository.findByIsPlaceholderTrue()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El jugador vacío no se encuentra en la base de datos."));

        // 5. Reemplazar el jugador en todos los rosters
        List<RosterPlayer> rosterEntries = rosterPlayerRepository.findAllByPlayerId(playerId);
        rosterEntries.forEach(entry -> entry.setPlayer(placeholderPlayer));
        rosterPlayerRepository.saveAll(rosterEntries);

        // 6. Eliminar el jugador
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

    @Transactional
    public PlayerResponseDto updatePlayerPoints(Long leagueId, Long playerId, PointsUpdateDto pointsUpdateDto, Long userId) {
        // 1. Verificación de permisos de administrador
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para actualizar los puntos de un jugador en esta liga.");
        }

        // 2. Obtener el jugador por su ID
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        // 3. Verificar que el jugador pertenece a la liga correcta
        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        // 4. Actualizar los puntos totales
        player.setTotalPoints(pointsUpdateDto.getTotalPoints());

        // 5. Guardar el jugador actualizado
        Player updatedPlayer = playerRepository.save(player);
        return mapToPlayerResponseDto(updatedPlayer);
    }

    private PlayerResponseDto mapToPlayerResponseDto(Player player) {
        return new PlayerResponseDto(player.getId(), player.getName(), player.getImage(), player.getTotalPoints());
    }
}