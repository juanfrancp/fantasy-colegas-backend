package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchStatsSubmissionDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchTeamResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.MatchStatus;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private PlayerMatchStatsRepository playerMatchStatsRepository;

    @Autowired
    private UserMatchLineupRepository userMatchLineupRepository;

    @Autowired
    private RosterPlayerRepository rosterPlayerRepository;

    @Autowired
    private PointsCalculationService pointsCalculationService;

    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto matchCreateDto) {
        League league = leagueRepository.findById(matchCreateDto.getLeagueId())
                .orElseThrow(() -> new EntityNotFoundException("League not found with id: " + matchCreateDto.getLeagueId()));

        List<Player> homePlayers = playerRepository.findAllById(matchCreateDto.getHomeTeamPlayerIds());
        List<Player> awayPlayers = playerRepository.findAllById(matchCreateDto.getAwayTeamPlayerIds());

        MatchTeam homeTeam = new MatchTeam(matchCreateDto.getHomeTeamName(), homePlayers);
        MatchTeam awayTeam = new MatchTeam(matchCreateDto.getAwayTeamName(), awayPlayers);

        Match match = new Match();
        match.setLeague(league);
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchDate(matchCreateDto.getMatchDate());

        Match savedMatch = matchRepository.save(match);
        return convertToDto(savedMatch);
    }

    public List<MatchResponseDto> getUpcomingMatches() {
        return matchRepository.findByMatchDateAfter(LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MatchResponseDto> getPastMatches() {
        return matchRepository.findByMatchDateBefore(LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Método auxiliar para convertir Match a DTO inyectando los puntos del partido
    private MatchResponseDto convertToDto(Match match) {
        MatchResponseDto dto = new MatchResponseDto();
        dto.setId(match.getId());
        dto.setLeagueId(match.getLeague().getId());
        dto.setMatchDate(match.getMatchDate());
        dto.setStatus(match.getStatus()); // Asegúrate de tener este campo en el DTO
        dto.setHomeScore(match.getHomeScore());
        dto.setAwayScore(match.getAwayScore());

        // 1. Recuperar TODAS las estadísticas de este partido de una sola vez
        List<PlayerMatchStats> allStats = playerMatchStatsRepository.findByMatch(match);

        // 2. Crear un mapa para búsqueda rápida: PlayerID -> Puntos Totales (Campo + Portero)
        Map<Long, Double> matchPointsMap = allStats.stream()
                .collect(Collectors.toMap(
                        stats -> stats.getPlayer().getId(),
                        stats -> stats.getTotalFieldPoints() + stats.getTotalGoalkeeperPoints()
                ));

        // 3. Convertir equipos inyectando los puntos específicos
        if (match.getHomeTeam() != null) {
            dto.setHomeTeam(convertTeamToDto(match.getHomeTeam(), matchPointsMap));
        }
        if (match.getAwayTeam() != null) {
            dto.setAwayTeam(convertTeamToDto(match.getAwayTeam(), matchPointsMap));
        }

        return dto;
    }

    // Método auxiliar para equipos
    private MatchTeamResponseDto convertTeamToDto(MatchTeam team, Map<Long, Double> pointsMap) {
        MatchTeamResponseDto teamDto = new MatchTeamResponseDto();
        teamDto.setId(team.getId());
        teamDto.setName(team.getName());

        List<PlayerResponseDto> playerDtos = team.getPlayers().stream().map(player -> {
            PlayerResponseDto pDto = convertPlayerToDto(player);

            Double matchPoints = pointsMap.getOrDefault(player.getId(), 0.0);

            pDto.setTotalPoints(matchPoints.intValue());

            return pDto;
        }).collect(Collectors.toList());

        teamDto.setPlayers(playerDtos);
        return teamDto;
    }

    // --- MÉTODO CORREGIDO ---
    private PlayerResponseDto convertPlayerToDto(Player player) {
        PlayerResponseDto dto = new PlayerResponseDto();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setImage(player.getImage());
        dto.setTotalPoints(player.getTotalPoints());
        return dto;
    }

    @Transactional
    public MatchResponseDto updateMatch(Long matchId, MatchUpdateDto updateDto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found with id: " + matchId));

        // Validar que el partido no se haya jugado ya
        if (match.getMatchDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot update a match that has already been played.");
        }

        // Actualizar equipos
        List<Player> homePlayers = playerRepository.findAllById(updateDto.getHomeTeamPlayerIds());
        List<Player> awayPlayers = playerRepository.findAllById(updateDto.getAwayTeamPlayerIds());

        match.getHomeTeam().setName(updateDto.getHomeTeamName());
        match.getHomeTeam().getPlayers().clear();
        match.getHomeTeam().getPlayers().addAll(homePlayers);

        match.getAwayTeam().setName(updateDto.getAwayTeamName());
        match.getAwayTeam().getPlayers().clear();
        match.getAwayTeam().getPlayers().addAll(awayPlayers);

        // Actualizar fecha del partido
        match.setMatchDate(updateDto.getMatchDate());

        Match updatedMatch = matchRepository.save(match);
        return convertToDto(updatedMatch);
    }

    @Transactional // IMPORTANTE: Asegúrate de que importas org.springframework.transaction.annotation.Transactional
    public MatchResponseDto submitMatchStats(Long matchId, MatchStatsSubmissionDto submissionDto) {
        // 1. Buscar el partido
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() == MatchStatus.SCHEDULED) {
            lockMatchAndSnapshot(matchId);
            // Recargamos el match porque el estado ha cambiado en BDD
            match = matchRepository.findById(matchId).orElseThrow();
        }

        // 2. Actualizar el marcador global del partido
        // Hibernate detectará esto como un "Dirty Check", pero usaremos saveAndFlush al final para asegurar.
        match.setHomeScore(submissionDto.getHomeScore());
        match.setAwayScore(submissionDto.getAwayScore());

        // 3. Procesar estadísticas individuales de los jugadores
        if (submissionDto.getPlayerStats() != null) {
            for (PlayerMatchStatsUpdateDto statDto : submissionDto.getPlayerStats()) {
                Player player = playerRepository.findById(statDto.getPlayerId())
                        .orElseThrow(() -> new EntityNotFoundException("Player not found with id: " + statDto.getPlayerId()));

                // Buscar o Crear
                PlayerMatchStats stats = playerMatchStatsRepository.findByMatchAndPlayer(match, player)
                        .orElse(new PlayerMatchStats());

                // Si es nuevo (ID nulo), asignamos las relaciones obligatorias
                if (stats.getId() == null) {
                    stats.setMatch(match);
                    stats.setPlayer(player);
                }

                // --- Mapeo de campos --
                stats.setGolesMarcados(statDto.getGolesMarcados());
                stats.setFallosClarosDeGol(statDto.getFallosClarosDeGol());
                stats.setAsistencias(statDto.getAsistencias());
                stats.setGolesEncajadosComoPortero(statDto.getGolesEncajadosComoPortero());
                stats.setParadasComoPortero(statDto.getParadasComoPortero());
                stats.setFaltasCometidas(statDto.getFaltasCometidas());
                stats.setFaltasRecibidas(statDto.getFaltasRecibidas());
                stats.setPenaltisRecibidos(statDto.getPenaltisRecibidos());
                stats.setPenaltisCometidos(statDto.getPenaltisCometidos());
                stats.setTarjetasAmarillas(statDto.getTarjetasAmarillas());
                stats.setTarjetasRojas(statDto.getTarjetasRojas());
                stats.setPorteriaImbatida(statDto.isPorteriaImbatida());

                // 1. Calculamos puntos como si fuera jugador de CAMPO
                double fieldPoints = pointsCalculationService.calculatePointsForRole(stats, PlayerTeamRole.CAMPO);
                stats.setTotalFieldPoints(fieldPoints);

                // 2. Calculamos puntos como si fuera PORTERO
                double gkPoints = pointsCalculationService.calculatePointsForRole(stats, PlayerTeamRole.PORTERO);
                stats.setTotalGoalkeeperPoints(gkPoints);

                // Forzamos el guardado inmediato de la estadística
                playerMatchStatsRepository.saveAndFlush(stats);
            }
        }

        // 4. Guardar y Forzar flush del partido
        Match savedMatch = matchRepository.saveAndFlush(match);

        return convertToDto(savedMatch);
    }

    @Transactional
    public void lockMatchAndSnapshot(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("El partido ya ha comenzado o finalizado.");
        }

        // 1. Cambiar estado
        match.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match);

        // 2. LA MAGIA: Copiar Rosters actuales a UserMatchLineup
        // Obtenemos todos los jugadores activos en esa liga
        List<RosterPlayer> currentRosters = rosterPlayerRepository.findAllByLeagueId(match.getLeague().getId());

        List<UserMatchLineup> snapshot = new ArrayList<>();
        for (RosterPlayer rp : currentRosters) {
            // Creamos la copia histórica
            UserMatchLineup lineup = new UserMatchLineup(
                    rp.getUser(),
                    match,
                    rp.getPlayer(),
                    rp.getRole() // Aquí guardamos si era PORTERO o CAMPO en este instante
            );
            snapshot.add(lineup);
        }

        userMatchLineupRepository.saveAll(snapshot);
    }
}