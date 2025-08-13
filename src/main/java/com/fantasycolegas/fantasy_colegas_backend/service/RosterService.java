package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterPlayerDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterPlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.model.RosterPlayer;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.RosterPlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RosterService {

    private final RosterPlayerRepository rosterPlayerRepository;
    private final LeagueService leagueService;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public RosterService(RosterPlayerRepository rosterPlayerRepository, LeagueService leagueService, LeagueRepository leagueRepository, PlayerRepository playerRepository, UserRepository userRepository) {
        this.rosterPlayerRepository = rosterPlayerRepository;
        this.leagueService = leagueService;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createRoster(Long leagueId, RosterCreateDto rosterCreateDto, Long userId) {
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden crear un equipo.");
        }

        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        int requestedSize = rosterCreateDto.getPlayers().size();
        if (requestedSize != league.getTeamSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tamaño del equipo debe ser " + league.getTeamSize() + ", pero se han enviado " + requestedSize + " jugadores.");
        }

        long porteroCount = rosterCreateDto.getPlayers().stream().filter(p -> p.getRole() == PlayerTeamRole.PORTERO).count();
        if (porteroCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo debe tener exactamente un portero.");
        }
        if (rosterCreateDto.getPlayers().stream().anyMatch(p -> p.getRole() == PlayerTeamRole.PORTERO) && rosterCreateDto.getPlayers().stream().anyMatch(p -> p.getRole() == PlayerTeamRole.CAMPO)) {
        }

        List<Long> playerIds = rosterCreateDto.getPlayers().stream().map(RosterPlayerDto::getPlayerId).collect(Collectors.toList());

        List<com.fantasycolegas.fantasy_colegas_backend.model.Player> existingPlayers = playerRepository.findAllById(playerIds);
        if (existingPlayers.size() != requestedSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jugadores no se encontraron.");
        }
        if (existingPlayers.stream().anyMatch(p -> !p.getLeague().getId().equals(leagueId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jugadores no pertenecen a esta liga.");
        }

        rosterPlayerRepository.deleteByUserIdAndLeagueId(userId, leagueId);

        List<RosterPlayer> rosterPlayers = rosterCreateDto.getPlayers().stream().map(rosterPlayerDto -> {
            RosterPlayer rosterPlayer = new RosterPlayer();
            rosterPlayer.setUser(user);
            rosterPlayer.setLeague(league);

            com.fantasycolegas.fantasy_colegas_backend.model.Player player = existingPlayers.stream().filter(p -> p.getId().equals(rosterPlayerDto.getPlayerId())).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jugador no encontrado en la lista de la liga."));

            rosterPlayer.setPlayer(player);
            rosterPlayer.setRole(rosterPlayerDto.getRole());
            return rosterPlayer;
        }).collect(Collectors.toList());

        rosterPlayerRepository.saveAll(rosterPlayers);

        return "Equipo de la jornada guardado con éxito.";
    }

    public List<RosterPlayerResponseDto> getUserRoster(Long leagueId, Long userId) {
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden ver su equipo.");
        }

        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);

        return rosterPlayers.stream().map(rosterPlayer -> new RosterPlayerResponseDto(rosterPlayer.getPlayer().getId(), rosterPlayer.getPlayer().getName(), rosterPlayer.getRole(), rosterPlayer.getPlayer().getImage(), rosterPlayer.getPlayer().getTotalPoints())).collect(Collectors.toList());
    }

    @Transactional
    public String removePlayerFromRoster(Long leagueId, Long userId, Long playerIdToRemove) {
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden modificar su equipo.");
        }

        List<RosterPlayer> roster = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);
        if (roster.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene un equipo en esta liga.");
        }

        Optional<RosterPlayer> playerToRemoveOpt = roster.stream().filter(rp -> rp.getPlayer().getId().equals(playerIdToRemove)).findFirst();

        if (playerToRemoveOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador a eliminar no se encuentra en tu equipo.");
        }

        RosterPlayer rosterPlayerToRemove = playerToRemoveOpt.get();
        Player playerToRemove = rosterPlayerToRemove.getPlayer();

        if (playerToRemove.isPlaceholder()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar al jugador vacío.");
        }

        Player placeholderPlayer = playerRepository.findByIsPlaceholderTrue().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador vacío no se encuentra en la base de datos. Contacta con el administrador."));

        rosterPlayerToRemove.setPlayer(placeholderPlayer);
        if (rosterPlayerToRemove.getRole() == PlayerTeamRole.PORTERO) {
            rosterPlayerToRemove.setRole(PlayerTeamRole.PORTERO);
        } else {
            rosterPlayerToRemove.setRole(PlayerTeamRole.CAMPO);
        }

        rosterPlayerRepository.save(rosterPlayerToRemove);

        return "Jugador eliminado y reemplazado con éxito.";
    }

    @Transactional
    public String addPlayerToRoster(Long leagueId, Long userId, Long playerIdToAdd, PlayerTeamRole position) {
        Player playerToAdd = playerRepository.findById(playerIdToAdd).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador a añadir no existe."));

        boolean playerAlreadyInRoster = rosterPlayerRepository.existsByUserIdAndLeagueIdAndPlayerId(userId, leagueId, playerIdToAdd);
        if (playerAlreadyInRoster) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador ya se encuentra en tu equipo.");
        }

        Player placeholderPlayer = playerRepository.findByIsPlaceholderTrue().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador vacío no se encuentra en la base de datos. Contacta con el administrador."));

        Optional<RosterPlayer> emptyPositionOpt;

        if (position == PlayerTeamRole.PORTERO) {
            emptyPositionOpt = rosterPlayerRepository.findByUserIdAndLeagueIdAndRoleAndPlayerId(userId, leagueId, PlayerTeamRole.PORTERO, placeholderPlayer.getId());
        } else {
            emptyPositionOpt = rosterPlayerRepository.findFirstByUserIdAndLeagueIdAndRoleAndPlayerId(userId, leagueId, PlayerTeamRole.CAMPO, placeholderPlayer.getId());
        }

        if (emptyPositionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay posiciones disponibles para el rol de " + position.name() + " en tu equipo.");
        }

        RosterPlayer emptyPosition = emptyPositionOpt.get();

        emptyPosition.setPlayer(playerToAdd);

        rosterPlayerRepository.save(emptyPosition);

        return "Jugador " + playerToAdd.getName() + " añadido a tu equipo con éxito.";
    }
}