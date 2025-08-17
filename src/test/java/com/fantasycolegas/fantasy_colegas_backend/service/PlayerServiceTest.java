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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private LeagueService leagueService;
    @Mock
    private RosterPlayerRepository rosterPlayerRepository;

    @InjectMocks
    private PlayerService playerService;

    private League league;
    private Player player;
    private final Long leagueId = 1L;
    private final Long nonExistentLeagueId = 99L;
    private final Long userId = 1L;
    private final Long playerId = 1L;
    private final Long nonExistentPlayerId = 99L;

    @BeforeEach
    void setUp() {
        league = new League();
        league.setId(leagueId);

        player = new Player();
        player.setId(playerId);
        player.setName("Test Player");
        player.setLeague(league);
    }

    @Test
    void createPlayer_whenUserIsAdmin_shouldCreatePlayer() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("New Player");

        PlayerResponseDto result = playerService.createPlayer(leagueId, createDto, userId);

        assertNotNull(result);
        assertEquals(player.getName(), result.getName());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void createPlayer_whenUserIsNotAdmin_shouldThrowForbiddenException() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(false);
        PlayerCreateDto createDto = new PlayerCreateDto();

        assertThrows(ResponseStatusException.class, () -> playerService.createPlayer(leagueId, createDto, userId));
    }

    @Test
    void createPlayer_whenLeagueNotFound_shouldThrowNotFoundException() {
        when(leagueService.checkIfUserIsAdmin(nonExistentLeagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(nonExistentLeagueId)).thenReturn(Optional.empty());

        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("New Player");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.createPlayer(nonExistentLeagueId, createDto, userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updatePlayer_whenUserIsAdmin_shouldUpdatePlayer() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("Updated Name");

        PlayerResponseDto result = playerService.updatePlayer(leagueId, playerId, updateDto, userId);

        assertEquals("Updated Name", result.getName());
    }

    @Test
    void updatePlayer_withNoChanges_shouldNotFail() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        PlayerUpdateDto updateDto = new PlayerUpdateDto();

        PlayerResponseDto result = playerService.updatePlayer(leagueId, playerId, updateDto, userId);

        assertEquals("Test Player", result.getName());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void updatePlayer_whenPlayerNotFound_shouldThrowNotFoundException() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(nonExistentPlayerId)).thenReturn(Optional.empty());

        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("Updated Name");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.updatePlayer(leagueId, nonExistentPlayerId, updateDto, userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updatePlayer_whenPlayerNotInLeague_shouldThrowBadRequest() {
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player.setLeague(anotherLeague);

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("Updated Name");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.updatePlayer(leagueId, playerId, updateDto, userId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deletePlayer_whenUserIsAdmin_shouldDeletePlayerAndReplaceInRosters() {
        Player placeholder = new Player();
        placeholder.setId(99L);
        placeholder.setPlaceholder(true);

        RosterPlayer rosterEntry = new RosterPlayer();
        rosterEntry.setPlayer(player);

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(placeholder));
        when(rosterPlayerRepository.findAllByPlayerId(playerId)).thenReturn(Collections.singletonList(rosterEntry));

        playerService.deletePlayer(leagueId, playerId, userId);

        verify(rosterPlayerRepository, times(1)).saveAll(any());
        verify(playerRepository, times(1)).delete(player);
        assertEquals(placeholder, rosterEntry.getPlayer());
    }

    @Test
    void deletePlayer_whenPlayerNotFound_shouldThrowNotFoundException() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(nonExistentPlayerId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.deletePlayer(leagueId, nonExistentPlayerId, userId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deletePlayer_whenPlaceholderPlayerNotFound_shouldThrowInternalServerError() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.deletePlayer(leagueId, playerId, userId));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void getPlayerById_whenPlayerExistsAndBelongsToLeague_shouldReturnPlayer() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        PlayerResponseDto result = playerService.getPlayerById(leagueId, playerId);

        assertNotNull(result);
        assertEquals(player.getId(), result.getId());
    }

    @Test
    void getPlayerById_whenPlayerDoesNotBelongToLeague_shouldThrowBadRequest() {
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player.setLeague(anotherLeague);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(ResponseStatusException.class, () -> playerService.getPlayerById(leagueId, playerId));
    }

    @Test
    void updatePlayerPoints_whenUserIsAdmin_shouldUpdatePoints() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArgument(0));

        PointsUpdateDto pointsDto = new PointsUpdateDto(100);

        PlayerResponseDto result = playerService.updatePlayerPoints(leagueId, playerId, pointsDto, userId);

        assertEquals(100, result.getTotalPoints());
    }

    @Test
    void updatePlayerPoints_whenPlayerNotInLeague_shouldThrowBadRequest() {
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player.setLeague(anotherLeague);

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        PointsUpdateDto pointsDto = new PointsUpdateDto(100);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.updatePlayerPoints(leagueId, playerId, pointsDto, userId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deletePlayer_whenPlayerNotInLeague_shouldThrowBadRequest() {
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player.setLeague(anotherLeague);

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.deletePlayer(leagueId, playerId, userId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(playerRepository, never()).delete(any(Player.class));
    }

    @Test
    void updatePlayer_whenUpdatingOnlyName_shouldOnlyChangeName() {
        player.setImage("initial_image.png");

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArgument(0));

        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setImage(null);

        PlayerResponseDto result = playerService.updatePlayer(leagueId, playerId, updateDto, userId);

        assertEquals("Updated Name", result.getName());
        assertEquals("initial_image.png", player.getImage());
    }

    @Test
    void updatePlayer_whenUpdatingOnlyImage_shouldOnlyChangeImage() {
        player.setName("Initial Name");

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArgument(0));

        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName(null);
        updateDto.setImage("updated_image.png");

        PlayerResponseDto result = playerService.updatePlayer(leagueId, playerId, updateDto, userId);

        assertEquals("updated_image.png", result.getImage());
        assertEquals("Initial Name", player.getName());
    }

    @Test
    void updatePlayer_withBlankStrings_shouldIgnoreChanges() {
        player.setName("Initial Name");
        player.setImage("initial_image.png");

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("  ");
        updateDto.setImage("");

        PlayerResponseDto result = playerService.updatePlayer(leagueId, playerId, updateDto, userId);

        assertEquals("Initial Name", result.getName());
        assertEquals("initial_image.png", result.getImage());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void deletePlayer_whenPlayerIsNotInAnyRoster_shouldSucceed() {
        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(rosterPlayerRepository.findAllByPlayerId(playerId)).thenReturn(Collections.emptyList());
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(new Player()));

        assertDoesNotThrow(() -> playerService.deletePlayer(leagueId, playerId, userId));

        verify(playerRepository, times(1)).delete(player);
        verify(rosterPlayerRepository, never()).saveAll(any());
    }

    @Test
    void deletePlayer_whenAttemptingToDeletePlaceholder_shouldThrowBadRequest() {
        Player placeholderPlayer = new Player();
        placeholderPlayer.setId(playerId);
        placeholderPlayer.setPlaceholder(true);
        placeholderPlayer.setLeague(league);

        when(leagueService.checkIfUserIsAdmin(leagueId, userId)).thenReturn(true);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(placeholderPlayer));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> playerService.deletePlayer(leagueId, playerId, userId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No se puede eliminar al jugador vacío"));

        verify(playerRepository, never()).delete(any(Player.class));
    }
}