package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.Match;
import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.MatchRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

    @InjectMocks
    private MatchService matchService;

    private League league;
    private Player player1, player2;
    private final Long leagueId = 1L;

    @BeforeEach
    void setUp() {
        league = new League();
        league.setId(leagueId);
        league.setName("Test League");

        player1 = new Player(1L, "Player One", "img1.png", 100, league, false);
        player2 = new Player(2L, "Player Two", "img2.png", 120, league, false);
    }

    @Test
    void createMatch_whenLeagueAndPlayersExist_shouldCreateMatch() {
        // Arrange
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(leagueId);
        createDto.setHomeTeamName("Home Team");
        createDto.setAwayTeamName("Away Team");
        createDto.setMatchDate(LocalDateTime.now().plusDays(1));
        createDto.setHomeTeamPlayerIds(List.of(1L));
        createDto.setAwayTeamPlayerIds(List.of(2L));

        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(playerRepository.findAllById(List.of(1L))).thenReturn(List.of(player1));
        when(playerRepository.findAllById(List.of(2L))).thenReturn(List.of(player2));
        // Mock the behavior of matchRepository.save
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match matchToSave = invocation.getArgument(0);
            matchToSave.setId(1L); // Simulate saving and getting an ID
            matchToSave.getHomeTeam().setId(10L);
            matchToSave.getAwayTeam().setId(11L);
            return matchToSave;
        });

        // Act
        MatchResponseDto result = matchService.createMatch(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("Home Team", result.getHomeTeam().getName());
        assertEquals("Away Team", result.getAwayTeam().getName());
        assertEquals(1, result.getHomeTeam().getPlayers().size());
        assertEquals("Player One", result.getHomeTeam().getPlayers().get(0).getName());
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void createMatch_whenLeagueNotFound_shouldThrowException() {
        // Arrange
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(99L); // Non-existent league
        when(leagueRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> matchService.createMatch(createDto));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void getUpcomingMatches_shouldReturnListOfFutureMatches() {
        // Arrange
        Match upcomingMatch = new Match();
        upcomingMatch.setId(1L);
        upcomingMatch.setMatchDate(LocalDateTime.now().plusDays(2));
        upcomingMatch.setHomeTeam(new com.fantasycolegas.fantasy_colegas_backend.model.MatchTeam("Team A", Collections.emptyList()));
        upcomingMatch.setAwayTeam(new com.fantasycolegas.fantasy_colegas_backend.model.MatchTeam("Team B", Collections.emptyList()));

        when(matchRepository.findByMatchDateAfter(any(LocalDateTime.class))).thenReturn(List.of(upcomingMatch));

        // Act
        List<MatchResponseDto> results = matchService.getUpcomingMatches();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(upcomingMatch.getId(), results.get(0).getId());
    }

    @Test
    void getPastMatches_shouldReturnListOfPastMatches() {
        // Arrange
        Match pastMatch = new Match();
        pastMatch.setId(2L);
        pastMatch.setMatchDate(LocalDateTime.now().minusDays(2));
        pastMatch.setHomeTeam(new com.fantasycolegas.fantasy_colegas_backend.model.MatchTeam("Team C", Collections.emptyList()));
        pastMatch.setAwayTeam(new com.fantasycolegas.fantasy_colegas_backend.model.MatchTeam("Team D", Collections.emptyList()));

        when(matchRepository.findByMatchDateBefore(any(LocalDateTime.class))).thenReturn(List.of(pastMatch));

        // Act
        List<MatchResponseDto> results = matchService.getPastMatches();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(pastMatch.getId(), results.get(0).getId());
    }
}