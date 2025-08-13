package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerMatchStatsResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Servicio para la gestión de partidos y estadísticas de jugadores.
 * <p>
 * Contiene la lógica de negocio para crear partidos, actualizar las estadísticas
 * de los jugadores en un partido y recalcular los puntos de los usuarios.
 * </p>
 */
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final PlayerMatchStatsRepository playerMatchStatsRepository;
    private final LeagueService leagueService;
    private final PointsCalculationService pointsCalculationService;
    private final RosterPlayerRepository rosterPlayerRepository;
    private final UserRepository userRepository;

    /**
     * Constructor del servicio que inyecta las dependencias de los repositorios.
     */
    public MatchService(MatchRepository matchRepository, LeagueRepository leagueRepository, PlayerRepository playerRepository, PlayerMatchStatsRepository playerMatchStatsRepository, LeagueService leagueService, PointsCalculationService pointsCalculationService, RosterPlayerRepository rosterPlayerRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.playerMatchStatsRepository = playerMatchStatsRepository;
        this.leagueService = leagueService;
        this.pointsCalculationService = pointsCalculationService;
        this.rosterPlayerRepository = rosterPlayerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea un nuevo partido en una liga específica.
     * <p>
     * El nombre del partido se genera automáticamente como "Partido jornada X",
     * donde X es el número de partidos existentes en la liga + 1.
     * </p>
     *
     * @param matchCreateDto DTO con los datos para la creación del partido.
     * @return Un {@link MatchResponseDto} con los detalles del partido creado.
     */
    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto matchCreateDto) {
        League league = leagueRepository.findById(matchCreateDto.getLeagueId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        long matchCount = matchRepository.countByLeagueId(league.getId());

        Match newMatch = new Match();
        newMatch.setLeague(league);
        newMatch.setMatchDate(matchCreateDto.getMatchDate());
        newMatch.setDescription(matchCreateDto.getDescription());
        newMatch.setName("Partido jornada " + (matchCount + 1));

        matchRepository.save(newMatch);

        MatchResponseDto responseDto = new MatchResponseDto();
        responseDto.setId(newMatch.getId());
        responseDto.setName(newMatch.getName());
        responseDto.setDescription(newMatch.getDescription());
        responseDto.setMatchDate(newMatch.getMatchDate());
        responseDto.setLeagueId(newMatch.getLeague().getId());
        responseDto.setLeagueName(newMatch.getLeague().getName());

        return responseDto;
    }

    /**
     * Actualiza las estadísticas de un jugador en un partido específico.
     * <p>
     * Si las estadísticas ya existen, se actualizan; de lo contrario, se crean nuevas.
     * También recalcula los puntos del jugador y actualiza los puntos del usuario en la liga.
     * </p>
     *
     * @param matchId        El ID del partido.
     * @param statsUpdateDto DTO con los datos de las estadísticas a actualizar.
     * @return Un {@link PlayerMatchStatsResponseDto} con los datos de las estadísticas actualizadas.
     */
    @Transactional
    public PlayerMatchStatsResponseDto updatePlayerStats(Long matchId, PlayerMatchStatsUpdateDto statsUpdateDto) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado."));

        Player player = playerRepository.findById(statsUpdateDto.getPlayerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        Optional<PlayerMatchStats> existingStats = playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, player.getId());
        PlayerMatchStats playerMatchStats = existingStats.orElseGet(PlayerMatchStats::new);

        playerMatchStats.setMatch(match);
        playerMatchStats.setPlayer(player);
        playerMatchStats.setGolesMarcados(statsUpdateDto.getGolesMarcados());
        playerMatchStats.setAsistencias(statsUpdateDto.getAsistencias());
        playerMatchStats.setFallosClarosDeGol(statsUpdateDto.getFallosClarosDeGol());
        playerMatchStats.setGolesEncajadosComoPortero(statsUpdateDto.getGolesEncajadosComoPortero());
        playerMatchStats.setParadasComoPortero(statsUpdateDto.getParadasComoPortero());
        playerMatchStats.setCesionesConcedidas(statsUpdateDto.getCesionesConcedidas());
        playerMatchStats.setFaltasCometidas(statsUpdateDto.getFaltasCometidas());
        playerMatchStats.setFaltasRecibidas(statsUpdateDto.getFaltasRecibidas());
        playerMatchStats.setPenaltisRecibidos(statsUpdateDto.getPenaltisRecibidos());
        playerMatchStats.setPenaltisCometidos(statsUpdateDto.getPenaltisCometidos());

        playerMatchStats.setPasesAcertados(statsUpdateDto.getPasesAcertados());
        playerMatchStats.setPasesFallados(statsUpdateDto.getPasesFallados());
        playerMatchStats.setRobosDeBalon(statsUpdateDto.getRobosDeBalon());
        playerMatchStats.setTirosCompletados(statsUpdateDto.getTirosCompletados());
        playerMatchStats.setTirosEntreLosTresPalos(statsUpdateDto.getTirosEntreLosTresPalos());
        playerMatchStats.setTiempoJugado(statsUpdateDto.getTiempoJugado());
        playerMatchStats.setTarjetasAmarillas(statsUpdateDto.getTarjetasAmarillas());
        playerMatchStats.setTarjetasRojas(statsUpdateDto.getTarjetasRojas());

        double calculatedFieldPoints = pointsCalculationService.calculatePointsForRole(statsUpdateDto, PlayerTeamRole.CAMPO);
        double calculatedGoalkeeperPoints = pointsCalculationService.calculatePointsForRole(statsUpdateDto, PlayerTeamRole.PORTERO);

        playerMatchStats.setTotalFieldPoints(calculatedFieldPoints);
        playerMatchStats.setTotalGoalkeeperPoints(calculatedGoalkeeperPoints);

        playerMatchStatsRepository.save(playerMatchStats);

        updateUserPoints(playerMatchStats, player.getLeague().getId());

        return new PlayerMatchStatsResponseDto(playerMatchStats.getId(), playerMatchStats.getPlayer().getId(), playerMatchStats.getGolesMarcados(), playerMatchStats.getFallosClarosDeGol(), playerMatchStats.getAsistencias(), playerMatchStats.getGolesEncajadosComoPortero(), playerMatchStats.getParadasComoPortero(), playerMatchStats.getCesionesConcedidas(), playerMatchStats.getFaltasCometidas(), playerMatchStats.getFaltasRecibidas(), playerMatchStats.getPenaltisRecibidos(), playerMatchStats.getPenaltisCometidos(), playerMatchStats.getPasesAcertados(), playerMatchStats.getPasesFallados(), playerMatchStats.getRobosDeBalon(), playerMatchStats.getTirosCompletados(), playerMatchStats.getTirosEntreLosTresPalos(), playerMatchStats.getTiempoJugado(), playerMatchStats.getTarjetasAmarillas(), playerMatchStats.getTarjetasRojas(), playerMatchStats.getTotalFieldPoints(), playerMatchStats.getTotalGoalkeeperPoints());
    }

    /**
     * Verifica si un usuario es administrador de la liga a la que pertenece un partido.
     *
     * @param matchId El ID del partido.
     * @param userId  El ID del usuario.
     * @return {@code true} si el usuario es administrador, {@code false} en caso contrario.
     */
    public boolean checkIfUserIsAdminOfMatchLeague(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado."));
        return leagueService.checkIfUserIsAdmin(match.getLeague().getId(), userId);
    }

    /**
     * Actualiza los puntos de los usuarios cuyos rosters contienen al jugador.
     * <p>
     * Recalcula los puntos totales del jugador y los suma a los usuarios que lo tienen en su equipo.
     * </p>
     *
     * @param playerMatchStats Las estadísticas del jugador en el partido.
     * @param leagueId         El ID de la liga.
     */
    @Transactional
    private void updateUserPoints(PlayerMatchStats playerMatchStats, Long leagueId) {
        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findByUserIdAndLeagueId(playerMatchStats.getPlayer().getId(), leagueId);

        for (RosterPlayer rosterPlayer : rosterPlayers) {
            User user = rosterPlayer.getUser();
            double pointsToAdd = 0.0;

            if (rosterPlayer.getRole() == PlayerTeamRole.CAMPO) {
                pointsToAdd = playerMatchStats.getTotalFieldPoints();
            } else if (rosterPlayer.getRole() == PlayerTeamRole.PORTERO) {
                pointsToAdd = playerMatchStats.getTotalGoalkeeperPoints();
            }

        }
    }
}