package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterPlayerDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterPlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.model.RosterPlayer;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.RosterPlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RosterService {

    private final RosterPlayerRepository rosterPlayerRepository;
    private final LeagueService leagueService;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public RosterService(RosterPlayerRepository rosterPlayerRepository, LeagueService leagueService,
                         LeagueRepository leagueRepository, PlayerRepository playerRepository,
                         UserRepository userRepository) {
        this.rosterPlayerRepository = rosterPlayerRepository;
        this.leagueService = leagueService;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createRoster(Long leagueId, RosterCreateDto rosterCreateDto, Long userId) {
        // 1. Validar que el usuario es miembro de la liga
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden crear un equipo.");
        }

        // 2. Obtener la liga y el usuario
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        // 3. Validar el tamaño del equipo
        int requestedSize = rosterCreateDto.getPlayers().size();
        if (requestedSize != league.getTeamSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tamaño del equipo debe ser " + league.getTeamSize() + ", pero se han enviado " + requestedSize + " jugadores.");
        }

        // 4. Validar la composición del equipo (1 portero, el resto campo)
        long porteroCount = rosterCreateDto.getPlayers().stream()
                .filter(p -> p.getRole() == PlayerTeamRole.PORTERO)
                .count();
        if (porteroCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo debe tener exactamente un portero.");
        }
        if (rosterCreateDto.getPlayers().stream().anyMatch(p -> p.getRole() == PlayerTeamRole.PORTERO) &&
                rosterCreateDto.getPlayers().stream().anyMatch(p -> p.getRole() == PlayerTeamRole.CAMPO)) {
            // Validación extra para asegurar que hay al menos un CAMPO si el tamaño lo permite
        }

        // 5. Validar que los jugadores existen y pertenecen a la liga
        List<Long> playerIds = rosterCreateDto.getPlayers().stream()
                .map(RosterPlayerDto::getPlayerId)
                .collect(Collectors.toList());

        List<com.fantasycolegas.fantasy_colegas_backend.model.Player> existingPlayers = playerRepository.findAllById(playerIds);
        if (existingPlayers.size() != requestedSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jugadores no se encontraron.");
        }
        // Verificar que todos los jugadores pertenecen a la liga correcta
        if (existingPlayers.stream().anyMatch(p -> !p.getLeague().getId().equals(leagueId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jugadores no pertenecen a esta liga.");
        }

        // 6. Si ya existe un equipo para este usuario en esta liga, lo eliminamos (versión por defecto para actualizar)
        rosterPlayerRepository.deleteByUserIdAndLeagueId(userId, leagueId);

        // 7. Mapear y guardar los nuevos jugadores del equipo
        List<RosterPlayer> rosterPlayers = rosterCreateDto.getPlayers().stream()
                .map(rosterPlayerDto -> {
                    RosterPlayer rosterPlayer = new RosterPlayer();
                    rosterPlayer.setUser(user);
                    rosterPlayer.setLeague(league);

                    // Asignar el jugador de la lista de existentes
                    com.fantasycolegas.fantasy_colegas_backend.model.Player player = existingPlayers.stream()
                            .filter(p -> p.getId().equals(rosterPlayerDto.getPlayerId()))
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jugador no encontrado en la lista de la liga."));

                    rosterPlayer.setPlayer(player);
                    rosterPlayer.setRole(rosterPlayerDto.getRole());
                    return rosterPlayer;
                })
                .collect(Collectors.toList());

        rosterPlayerRepository.saveAll(rosterPlayers);

        return "Equipo de la jornada guardado con éxito.";
    }

    public List<RosterPlayerResponseDto> getUserRoster(Long leagueId, Long userId) {
        // 1. Validar que el usuario es miembro de la liga
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden ver su equipo.");
        }

        // 2. Obtener la lista de RosterPlayer para el usuario y la liga
        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);

        // 3. Mapear a DTOs de respuesta
        return rosterPlayers.stream()
                .map(rosterPlayer -> new RosterPlayerResponseDto(
                        rosterPlayer.getPlayer().getId(),
                        rosterPlayer.getPlayer().getName(),
                        rosterPlayer.getRole(),
                        rosterPlayer.getPlayer().getImage(),
                        rosterPlayer.getPlayer().getTotalPoints()
                ))
                .collect(Collectors.toList());
    }
}