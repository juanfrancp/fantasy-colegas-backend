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
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para modificar jugadores en esta liga.");
        }

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        if (playerUpdateDto.getName() != null && !playerUpdateDto.getName().isBlank()) {
            player.setName(playerUpdateDto.getName());
        }
        if (playerUpdateDto.getImage() != null && !playerUpdateDto.getImage().isBlank()) {
            player.setImage(playerUpdateDto.getImage());
        }

        Player updatedPlayer = playerRepository.save(player);
        return mapToPlayerResponseDto(updatedPlayer);
    }

    @Transactional
    public PlayerResponseDto createPlayer(Long leagueId, PlayerCreateDto playerCreateDto, Long userId) {
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para añadir jugadores a esta liga.");
        }

        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        Player player = new Player();
        player.setName(playerCreateDto.getName());
        player.setLeague(league);
        player.setTotalPoints(0);

        if (playerCreateDto.getImage() != null && !playerCreateDto.getImage().isBlank()) {
            player.setImage(playerCreateDto.getImage());
        } else {
            player.setImage("https://example.com/default-player.jpg");
        }

        Player savedPlayer = playerRepository.save(player);
        return mapToPlayerResponseDto(savedPlayer);
    }

    @Transactional
    public void deletePlayer(Long leagueId, Long playerId, Long userId) {
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para eliminar jugadores en esta liga.");
        }

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        Player placeholderPlayer = playerRepository.findByIsPlaceholderTrue().orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El jugador vacío no se encuentra en la base de datos."));

        List<RosterPlayer> rosterEntries = rosterPlayerRepository.findAllByPlayerId(playerId);
        rosterEntries.forEach(entry -> entry.setPlayer(placeholderPlayer));
        rosterPlayerRepository.saveAll(rosterEntries);

        playerRepository.delete(player);
    }

    public PlayerResponseDto getPlayerById(Long leagueId, Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        return mapToPlayerResponseDto(player);
    }

    @Transactional
    public PlayerResponseDto updatePlayerPoints(Long leagueId, Long playerId, PointsUpdateDto pointsUpdateDto, Long userId) {
        if (!leagueService.checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para actualizar los puntos de un jugador en esta liga.");
        }

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        if (!player.getLeague().getId().equals(leagueId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a la liga especificada.");
        }

        player.setTotalPoints(pointsUpdateDto.getTotalPoints());

        Player updatedPlayer = playerRepository.save(player);
        return mapToPlayerResponseDto(updatedPlayer);
    }

    private PlayerResponseDto mapToPlayerResponseDto(Player player) {
        return new PlayerResponseDto(player.getId(), player.getName(), player.getImage(), player.getTotalPoints());
    }
}