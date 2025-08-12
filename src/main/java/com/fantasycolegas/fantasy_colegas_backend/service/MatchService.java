// Archivo: com/fantasycolegas/fantasy_colegas_backend/service/MatchService.java
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

    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto matchCreateDto) {
        League league = leagueRepository.findById(matchCreateDto.getLeagueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        // Obtener el número de partidos ya creados en la liga para el nombre
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

    @Transactional
    public PlayerMatchStatsResponseDto updatePlayerStats(Long matchId, PlayerMatchStatsUpdateDto statsUpdateDto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado."));

        Player player = playerRepository.findById(statsUpdateDto.getPlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

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

        // Opcionales
        playerMatchStats.setPasesAcertados(statsUpdateDto.getPasesAcertados());
        playerMatchStats.setPasesFallados(statsUpdateDto.getPasesFallados());
        playerMatchStats.setRobosDeBalon(statsUpdateDto.getRobosDeBalon());
        playerMatchStats.setTirosCompletados(statsUpdateDto.getTirosCompletados());
        playerMatchStats.setTirosEntreLosTresPalos(statsUpdateDto.getTirosEntreLosTresPalos());
        playerMatchStats.setTiempoJugado(statsUpdateDto.getTiempoJugado());
        playerMatchStats.setTarjetasAmarillas(statsUpdateDto.getTarjetasAmarillas());
        playerMatchStats.setTarjetasRojas(statsUpdateDto.getTarjetasRojas());

        // --- LÓGICA DE CÁLCULO DE PUNTOS ---
        // 1. Calcular los puntos de campo y portero por separado
        double calculatedFieldPoints = pointsCalculationService.calculatePointsForRole(statsUpdateDto, PlayerTeamRole.CAMPO);
        double calculatedGoalkeeperPoints = pointsCalculationService.calculatePointsForRole(statsUpdateDto, PlayerTeamRole.PORTERO);

        // 2. Asignar los puntos calculados a la entidad antes de guardarla
        playerMatchStats.setTotalFieldPoints(calculatedFieldPoints);
        playerMatchStats.setTotalGoalkeeperPoints(calculatedGoalkeeperPoints);

        // 3. Guardar la entidad actualizada en la base de datos
        playerMatchStatsRepository.save(playerMatchStats);

        // 4. Actualizar los puntos de los usuarios
        updateUserPoints(playerMatchStats, player.getLeague().getId());
        // --- FIN DEL CÁLCULO Y ACTUALIZACIÓN ---

        return new PlayerMatchStatsResponseDto(
                playerMatchStats.getId(),
                playerMatchStats.getPlayer().getId(),
                playerMatchStats.getGolesMarcados(),
                playerMatchStats.getFallosClarosDeGol(),
                playerMatchStats.getAsistencias(),
                playerMatchStats.getGolesEncajadosComoPortero(),
                playerMatchStats.getParadasComoPortero(),
                playerMatchStats.getCesionesConcedidas(),
                playerMatchStats.getFaltasCometidas(),
                playerMatchStats.getFaltasRecibidas(),
                playerMatchStats.getPenaltisRecibidos(),
                playerMatchStats.getPenaltisCometidos(),
                playerMatchStats.getPasesAcertados(),
                playerMatchStats.getPasesFallados(),
                playerMatchStats.getRobosDeBalon(),
                playerMatchStats.getTirosCompletados(),
                playerMatchStats.getTirosEntreLosTresPalos(),
                playerMatchStats.getTiempoJugado(),
                playerMatchStats.getTarjetasAmarillas(),
                playerMatchStats.getTarjetasRojas(),
                playerMatchStats.getTotalFieldPoints(),
                playerMatchStats.getTotalGoalkeeperPoints()
        );
    }

    public boolean checkIfUserIsAdminOfMatchLeague(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado."));
        return leagueService.checkIfUserIsAdmin(match.getLeague().getId(), userId);
    }

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

            // Actualizar los puntos del usuario (asumiendo que hay un campo `totalPoints` en la entidad `User`)
            // user.setTotalPoints(user.getTotalPoints() + pointsToAdd);
            // userRepository.save(user); // Guarda el usuario con los puntos actualizados

            // La lógica anterior es para un contador global, si quieres un contador por liga
            // deberás modificar las entidades User y UserLeagueRoles para que lo permitan
        }
    }
}