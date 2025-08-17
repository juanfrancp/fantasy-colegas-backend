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
        // Arrange
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);
        updateDto.setGolesMarcados(2); // Goles que dan puntos

        // Mock de las dependencias necesarias que causaban el error
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId)).thenReturn(Optional.empty());

        // Simular que el cálculo de puntos devuelve valores diferentes para cada rol
        when(pointsCalculationService.calculatePointsForRole(any(PlayerMatchStatsUpdateDto.class), eq(PlayerTeamRole.CAMPO))).thenReturn(10.0);
        when(pointsCalculationService.calculatePointsForRole(any(PlayerMatchStatsUpdateDto.class), eq(PlayerTeamRole.PORTERO))).thenReturn(3.0);

        // Act
        matchService.updatePlayerStats(matchId, updateDto);

        // Assert
        // Capturar el argumento guardado para verificar que los puntos se calcularon y asignaron correctamente
        ArgumentCaptor<PlayerMatchStats> statsCaptor = ArgumentCaptor.forClass(PlayerMatchStats.class);
        verify(playerMatchStatsRepository, times(1)).save(statsCaptor.capture());

        PlayerMatchStats savedStats = statsCaptor.getValue();
        assertEquals(10.0, savedStats.getTotalFieldPoints());
        assertEquals(3.0, savedStats.getTotalGoalkeeperPoints());
    }

    @Test
    void updatePlayerStats_whenStatsAlreadyExist_shouldUpdateExistingStats() {
        // Arrange
        PlayerMatchStats existingStats = new PlayerMatchStats();
        existingStats.setId(100L); // ID existente
        existingStats.setGolesMarcados(1); // Valor antiguo

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);
        updateDto.setGolesMarcados(2); // Nuevo valor

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId)).thenReturn(Optional.of(existingStats));

        // Act
        matchService.updatePlayerStats(matchId, updateDto);

        // Assert
        ArgumentCaptor<PlayerMatchStats> statsCaptor = ArgumentCaptor.forClass(PlayerMatchStats.class);
        verify(playerMatchStatsRepository, times(1)).save(statsCaptor.capture());

        // Verifica que se está actualizando la entidad existente y no creando una nueva
        assertEquals(existingStats.getId(), statsCaptor.getValue().getId());
        // Verifica que el valor se ha actualizado correctamente
        assertEquals(2, statsCaptor.getValue().getGolesMarcados());
    }

    @Test
    void checkIfUserIsAdminOfMatchLeague_whenMatchNotFound_shouldThrowException() {
        // Arrange
        Long nonExistentMatchId = 99L;
        when(matchRepository.findById(nonExistentMatchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> matchService.checkIfUserIsAdminOfMatchLeague(nonExistentMatchId, userId));
    }

    @Test
    void createMatch_whenLeagueHasExistingMatches_shouldCreateMatchWithCorrectName() {
        // Arrange: Simulamos que ya existen 5 partidos en la liga
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchRepository.countByLeagueId(leagueId)).thenReturn(5L);

        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(leagueId);
        createDto.setMatchDate(LocalDate.now());

        // Act
        MatchResponseDto result = matchService.createMatch(createDto);

        // Assert: Verificamos que el nuevo partido se llama correctamente "Partido jornada 6"
        assertNotNull(result);
        assertEquals("Partido jornada 6", result.getName());
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void updatePlayerStats_whenPlayerNotInMatchLeague_shouldThrowException() {
        // Arrange
        // Creamos una liga diferente para el jugador
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player.setLeague(anotherLeague); // Asignamos el jugador a la otra liga

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // Act & Assert
        // Verificamos que se lanza la excepción correcta
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El jugador no pertenece a la liga de este partido."));
    }

    @Test
    void updatePlayerStats_whenConcurrentAccess_shouldHandleGracefully() {
        // Arrange
        PlayerMatchStats existingStats = new PlayerMatchStats();
        existingStats.setId(100L);
        existingStats.setGolesMarcados(1);

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);
        updateDto.setGolesMarcados(2);

        // Simulamos que entre que se busca la estadística y se va a guardar, otro proceso ya la ha creado.
        // Primero, la búsqueda no encuentra nada.
        when(playerMatchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId))
                .thenReturn(Optional.empty()) // La primera llamada no encuentra nada
                .thenReturn(Optional.of(existingStats)); // Una segunda llamada sí lo encontraría

        // Al no encontrarla, se llamará a save con un objeto nuevo.
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerMatchStatsRepository.save(any(PlayerMatchStats.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        matchService.updatePlayerStats(matchId, updateDto);

        // Assert
        // Verificamos que aunque la primera búsqueda falló (simulando concurrencia),
        // el resultado final es una única operación de guardado.
        verify(playerMatchStatsRepository, times(1)).save(any(PlayerMatchStats.class));
    }

    @Test
    void updatePlayerStats_whenPlayerHasNoLeague_shouldThrowException() {
        // Arrange
        player.setLeague(null); // El jugador no tiene liga asignada

        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(playerId);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.updatePlayerStats(matchId, updateDto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El jugador no pertenece a la liga de este partido."));
    }
}