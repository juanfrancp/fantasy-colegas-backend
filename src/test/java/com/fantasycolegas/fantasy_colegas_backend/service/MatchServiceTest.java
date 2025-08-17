package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerMatchStatsResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerMatchStatsRepository playerMatchStatsRepository;
    @Mock
    private LeagueService leagueService;
    @Mock
    private PointsCalculationService pointsCalculationService;
    @Mock
    private RosterPlayerRepository rosterPlayerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchService matchService;

    private League league;
    private Match match;
    private Player player;
    private User user;
    private final Long leagueId = 1L;
    private final Long matchId = 1L;
    private final Long playerId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        league = new League();
        league.setId(leagueId);
        league.setName("Test League");

        match = new Match();
        match.setId(matchId);
        match.setLeague(league);
        match.setMatchDate(LocalDate.now().minusDays(1));

        player = new Player();
        player.setId(playerId);
        player.setLeague(league);

        user = new User();
        user.setId(userId);
    }

    @Test
    void createMatch_whenLeagueExists_shouldCreateMatch() {
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchRepository.countByLeagueId(leagueId)).thenReturn(0L);

        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(leagueId);
        createDto.setMatchDate(LocalDate.now());

        MatchResponseDto result = matchService.createMatch(createDto);

        assertNotNull(result);
        assertEquals("Partido jornada 1", result.getName());
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void createMatch_whenLeagueNotFound_shouldThrowException() {
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.empty());
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(leagueId);

        assertThrows(ResponseStatusException.class, () -> matchService.createMatch(createDto));
    }

    @Test
    void updatePlayerStats_whenMatchAndPlayerExist_shouldUpdateStats() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId)).thenReturn(Optional.empty());
        when(pointsCalculationService.calculatePointsForRole(any(), any(PlayerTeamRole.class))).thenReturn(10.0);

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        PlayerMatchStatsResponseDto result = matchService.updatePlayerStats(matchId, updateDto);

        assertNotNull(result);
        assertEquals(10.0, result.getTotalFieldPoints());
        verify(playerMatchStatsRepository, times(1)).save(any(PlayerMatchStats.class));
    }

    @Test
    void updatePlayerStats_whenMatchNotFound_shouldThrowException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        assertThrows(ResponseStatusException.class, () -> matchService.updatePlayerStats(matchId, updateDto));
    }

    @Test
    void updatePlayerStats_whenPlayerNotFound_shouldThrowException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        assertThrows(ResponseStatusException.class, () -> matchService.updatePlayerStats(matchId, updateDto));
    }

    @Test
    void checkIfUserIsAdminOfMatchLeague_whenUserIsAdmin_shouldReturnTrue() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);

        assertTrue(matchService.checkIfUserIsAdminOfMatchLeague(matchId, userId));
    }

    @Test
    void checkIfUserIsAdminOfMatchLeague_whenUserIsNotAdmin_shouldReturnFalse() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(false);

        assertFalse(matchService.checkIfUserIsAdminOfMatchLeague(matchId, userId));
    }

    @Test
    void updatePlayerStats_shouldCalculateAndSavePointsForAllRoles() {
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);
        updateDto.setGolesMarcados(2);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId)).thenReturn(Optional.empty());

        when(pointsCalculationService.calculatePointsForRole(any(PlayerMatchStatsUpdateDto.class), eq(PlayerTeamRole.CAMPO))).thenReturn(10.0);
        when(pointsCalculationService.calculatePointsForRole(any(PlayerMatchStatsUpdateDto.class), eq(PlayerTeamRole.PORTERO))).thenReturn(3.0);

        matchService.updatePlayerStats(matchId, updateDto);

        ArgumentCaptor<PlayerMatchStats> statsCaptor = ArgumentCaptor.forClass(PlayerMatchStats.class);
        verify(playerMatchStatsRepository, times(1)).save(statsCaptor.capture());

        PlayerMatchStats savedStats = statsCaptor.getValue();
        assertEquals(10.0, savedStats.getTotalFieldPoints());
        assertEquals(3.0, savedStats.getTotalGoalkeeperPoints());
    }

    @Test
    void updatePlayerStats_whenStatsAlreadyExist_shouldUpdateExistingStats() {
        PlayerMatchStats existingStats = new PlayerMatchStats();
        existingStats.setId(100L);
        existingStats.setGolesMarcados(1);

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);
        updateDto.setGolesMarcados(2);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId)).thenReturn(Optional.of(existingStats));

        matchService.updatePlayerStats(matchId, updateDto);

        ArgumentCaptor<PlayerMatchStats> statsCaptor = ArgumentCaptor.forClass(PlayerMatchStats.class);
        verify(playerMatchStatsRepository, times(1)).save(statsCaptor.capture());

        assertEquals(existingStats.getId(), statsCaptor.getValue().getId());
        assertEquals(2, statsCaptor.getValue().getGolesMarcados());
    }

    @Test
    void checkIfUserIsAdminOfMatchLeague_whenMatchNotFound_shouldThrowException() {
        Long nonExistentMatchId = 99L;
        when(matchRepository.findById(nonExistentMatchId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> matchService.checkIfUserIsAdminOfMatchLeague(nonExistentMatchId, userId));
    }

    @Test
    void createMatch_whenLeagueHasExistingMatches_shouldCreateMatchWithCorrectName() {
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchRepository.countByLeagueId(leagueId)).thenReturn(5L);

        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(leagueId);
        createDto.setMatchDate(LocalDate.now());

        MatchResponseDto result = matchService.createMatch(createDto);

        assertNotNull(result);
        assertEquals("Partido jornada 6", result.getName());
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void updatePlayerStats_whenPlayerNotInMatchLeague_shouldThrowException() {
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player.setLeague(anotherLeague);

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El jugador no pertenece a la liga de este partido."));
    }

    @Test
    void updatePlayerStats_whenConcurrentAccess_shouldHandleGracefully() {
        PlayerMatchStats existingStats = new PlayerMatchStats();
        existingStats.setId(100L);
        existingStats.setGolesMarcados(1);

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);
        updateDto.setGolesMarcados(2);

        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingStats));

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.save(any(PlayerMatchStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchService.updatePlayerStats(matchId, updateDto);

        verify(playerMatchStatsRepository, times(1)).save(any(PlayerMatchStats.class));
    }

    @Test
    void updatePlayerStats_whenPlayerHasNoLeague_shouldThrowException() {
        player.setLeague(null);

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El jugador no pertenece a la liga de este partido."));
    }

    @Test
    void updatePlayerStats_whenPlayerIsPlaceholder_shouldThrowException() {
        player.setPlaceholder(true);
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No se pueden asignar estadísticas al jugador vacío."));
    }

    @Test
    void updatePlayerStats_whenMatchIsInTheFuture_shouldThrowException() {
        match.setMatchDate(LocalDate.now().plusDays(1));
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No se pueden registrar estadísticas de un partido que aún no se ha jugado."));
    }

    @Test
    void updatePlayerStats_whenDatabaseSaveFails_shouldRollbackTransaction() {
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId)).thenReturn(Optional.empty());

        when(playerMatchStatsRepository.save(any(PlayerMatchStats.class)))
                .thenThrow(new RuntimeException("Error simulado de base de datos"));

        assertThrows(RuntimeException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));
    }
}