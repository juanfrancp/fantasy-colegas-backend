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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RosterServiceTest {

    @Mock
    private RosterPlayerRepository rosterPlayerRepository;
    @Mock
    private LeagueService leagueService;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RosterService rosterService;

    private User user;
    private League league;
    private Player player1, player2, placeholderPlayer;
    private final Long leagueId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);

        league = new League();
        league.setId(leagueId);
        league.setTeamSize(2);

        player1 = new Player();
        player1.setId(10L);
        player1.setName("Portero");
        player1.setLeague(league);

        player2 = new Player();
        player2.setId(20L);
        player2.setName("Jugador Campo");
        player2.setLeague(league);

        placeholderPlayer = new Player();
        placeholderPlayer.setId(99L);
        placeholderPlayer.setPlaceholder(true);
    }

    @Test
    void createRoster_whenSuccessful_shouldSaveRoster() {
        RosterCreateDto createDto = new RosterCreateDto();
        List<RosterPlayerDto> players = new ArrayList<>();
        players.add(createPlayerDto(player1.getId(), PlayerTeamRole.PORTERO));
        players.add(createPlayerDto(player2.getId(), PlayerTeamRole.CAMPO));
        createDto.setPlayers(players);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.findAllById(anyList())).thenReturn(List.of(player1, player2));

        String result = rosterService.createRoster(leagueId, createDto, userId);

        assertEquals("Equipo de la jornada guardado con éxito.", result);
        verify(rosterPlayerRepository, times(1)).deleteByUserIdAndLeagueId(userId, leagueId);
        verify(rosterPlayerRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createRoster_whenUserIsNotParticipant_shouldThrowForbiddenException() {
        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> rosterService.createRoster(leagueId, new RosterCreateDto(), userId));
    }

    @Test
    void createRoster_whenTeamSizeIsIncorrect_shouldThrowBadRequest() {
        league.setTeamSize(3);
        RosterCreateDto createDto = new RosterCreateDto();
        createDto.setPlayers(List.of(new RosterPlayerDto(), new RosterPlayerDto()));

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> rosterService.createRoster(leagueId, createDto, userId));
    }

    @Test
    void createRoster_whenGoalkeeperCountIsNotOne_shouldThrowBadRequest() {
        RosterCreateDto createDto = new RosterCreateDto();
        List<RosterPlayerDto> players = new ArrayList<>();
        players.add(createPlayerDto(player1.getId(), PlayerTeamRole.CAMPO));
        players.add(createPlayerDto(player2.getId(), PlayerTeamRole.CAMPO));
        createDto.setPlayers(players);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> rosterService.createRoster(leagueId, createDto, userId));
    }

    @Test
    void getUserRoster_whenRosterExists_shouldReturnRoster() {
        RosterPlayer rp1 = new RosterPlayer();
        rp1.setPlayer(player1);
        rp1.setRole(PlayerTeamRole.PORTERO);
        RosterPlayer rp2 = new RosterPlayer();
        rp2.setPlayer(player2);
        rp2.setRole(PlayerTeamRole.CAMPO);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId)).thenReturn(List.of(rp1, rp2));

        List<RosterPlayerResponseDto> result = rosterService.getUserRoster(leagueId, userId);

        assertEquals(2, result.size());
        assertEquals(player1.getName(), result.stream().filter(p -> p.getPlayerId().equals(player1.getId())).findFirst().get().getPlayerName());
    }

    @Test
    void removePlayerFromRoster_whenPlayerInRoster_shouldReplaceWithPlaceholder() {
        RosterPlayer playerToRemove = new RosterPlayer();
        playerToRemove.setPlayer(player2);
        playerToRemove.setRole(PlayerTeamRole.CAMPO);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId)).thenReturn(List.of(playerToRemove));
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(placeholderPlayer));

        String result = rosterService.removePlayerFromRoster(leagueId, userId, player2.getId());

        assertEquals("Jugador eliminado y reemplazado con éxito.", result);
        verify(rosterPlayerRepository, times(1)).save(playerToRemove);
        assertEquals(placeholderPlayer.getId(), playerToRemove.getPlayer().getId());
    }

    @Test
    void addPlayerToRoster_whenEmptySlotExists_shouldAddPlayer() {
        RosterPlayer emptyPosition = new RosterPlayer();
        emptyPosition.setPlayer(placeholderPlayer);
        emptyPosition.setRole(PlayerTeamRole.CAMPO);

        when(playerRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
        when(rosterPlayerRepository.existsByUserIdAndLeagueIdAndPlayerId(userId, leagueId, player2.getId())).thenReturn(false);
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(placeholderPlayer));
        when(rosterPlayerRepository.findFirstByUserIdAndLeagueIdAndRoleAndPlayerId(userId, leagueId, PlayerTeamRole.CAMPO, placeholderPlayer.getId()))
                .thenReturn(Optional.of(emptyPosition));

        String result = rosterService.addPlayerToRoster(leagueId, userId, player2.getId(), PlayerTeamRole.CAMPO);

        assertEquals("Jugador " + player2.getName() + " añadido a tu equipo con éxito.", result);
        verify(rosterPlayerRepository, times(1)).save(emptyPosition);
        assertEquals(player2.getId(), emptyPosition.getPlayer().getId());
    }

    @Test
    void addPlayerToRoster_whenPlayerAlreadyInRoster_shouldThrowBadRequest() {
        when(playerRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
        when(rosterPlayerRepository.existsByUserIdAndLeagueIdAndPlayerId(userId, leagueId, player2.getId())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> rosterService.addPlayerToRoster(leagueId, userId, player2.getId(), PlayerTeamRole.CAMPO));
    }

    @Test
    void addPlayerToRoster_whenNoEmptySlots_shouldThrowBadRequest() {
        when(playerRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
        when(rosterPlayerRepository.existsByUserIdAndLeagueIdAndPlayerId(userId, leagueId, player2.getId())).thenReturn(false);
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(placeholderPlayer));
        when(rosterPlayerRepository.findFirstByUserIdAndLeagueIdAndRoleAndPlayerId(userId, leagueId, PlayerTeamRole.CAMPO, placeholderPlayer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> rosterService.addPlayerToRoster(leagueId, userId, player2.getId(), PlayerTeamRole.CAMPO));
    }

    private RosterPlayerDto createPlayerDto(Long playerId, PlayerTeamRole role) {
        RosterPlayerDto dto = new RosterPlayerDto();
        dto.setPlayerId(playerId);
        dto.setRole(role);
        return dto;
    }

    @Test
    void createRoster_whenPlayerNotFound_shouldThrowBadRequest() {
        RosterCreateDto createDto = new RosterCreateDto();
        List<RosterPlayerDto> players = new ArrayList<>();
        players.add(createPlayerDto(player1.getId(), PlayerTeamRole.PORTERO));
        players.add(createPlayerDto(999L, PlayerTeamRole.CAMPO));
        createDto.setPlayers(players);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.findAllById(anyList())).thenReturn(List.of(player1));

        var exception = assertThrows(ResponseStatusException.class, () -> {
            rosterService.createRoster(leagueId, createDto, userId);
        });
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Uno o más jugadores no se encontraron"));
    }

    @Test
    void createRoster_whenPlayerBelongsToAnotherLeague_shouldThrowBadRequest() {
        League anotherLeague = new League();
        anotherLeague.setId(2L);
        player2.setLeague(anotherLeague);

        RosterCreateDto createDto = new RosterCreateDto();
        List<RosterPlayerDto> players = new ArrayList<>();
        players.add(createPlayerDto(player1.getId(), PlayerTeamRole.PORTERO));
        players.add(createPlayerDto(player2.getId(), PlayerTeamRole.CAMPO));
        createDto.setPlayers(players);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.findAllById(anyList())).thenReturn(List.of(player1, player2));

        var exception = assertThrows(ResponseStatusException.class, () -> {
            rosterService.createRoster(leagueId, createDto, userId);
        });
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Uno o más jugadores no pertenecen a esta liga"));
    }

    @Test
    void removePlayerFromRoster_whenPlayerIsNotInRoster_shouldThrowNotFound() {
        RosterPlayer playerInRoster = new RosterPlayer();
        playerInRoster.setPlayer(player1);
        playerInRoster.setRole(PlayerTeamRole.PORTERO);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId)).thenReturn(List.of(playerInRoster));

        var exception = assertThrows(ResponseStatusException.class, () -> {
            rosterService.removePlayerFromRoster(leagueId, userId, player2.getId());
        });
        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void removePlayerFromRoster_whenTryingToRemovePlaceholder_shouldThrowBadRequest() {
        RosterPlayer placeholderInRoster = new RosterPlayer();
        placeholderInRoster.setPlayer(placeholderPlayer);
        placeholderInRoster.setRole(PlayerTeamRole.CAMPO);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId)).thenReturn(List.of(placeholderInRoster));

        var exception = assertThrows(ResponseStatusException.class, () -> {
            rosterService.removePlayerFromRoster(leagueId, userId, placeholderPlayer.getId());
        });
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("No puedes eliminar al jugador vacío"));
    }

    @Test
    void removePlayerFromRoster_whenPlaceholderPlayerIsMissing_shouldThrowException() {
        RosterPlayer playerToRemove = new RosterPlayer();
        playerToRemove.setPlayer(player2);
        playerToRemove.setRole(PlayerTeamRole.CAMPO);

        when(leagueService.isUserParticipant(leagueId, userId)).thenReturn(true);
        when(rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId)).thenReturn(List.of(playerToRemove));
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class, () -> {
            rosterService.removePlayerFromRoster(leagueId, userId, player2.getId());
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("El jugador vacío no se encuentra en la base de datos"));
    }

    @Test
    void addPlayerToRoster_whenPlaceholderPlayerIsMissing_shouldThrowException() {
        when(playerRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
        when(rosterPlayerRepository.existsByUserIdAndLeagueIdAndPlayerId(userId, leagueId, player2.getId())).thenReturn(false);
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class, () -> {
            rosterService.addPlayerToRoster(leagueId, userId, player2.getId(), PlayerTeamRole.CAMPO);
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("El jugador vacío no se encuentra en la base de datos"));
    }
}