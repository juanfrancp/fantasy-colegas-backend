package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueTeamSizeUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserLeagueRoleRepository userLeagueRoleRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private RosterPlayerRepository rosterPlayerRepository;
    @Mock
    private LeagueJoinRequestRepository leagueJoinRequestRepository;
    @Mock
    private PlayerMatchStatsRepository playerMatchStatsRepository;
    @InjectMocks
    private LeagueService leagueService;

    private User testUser;
    private LeagueCreateDto leagueCreateDto;
    private User targetUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("Mi Liga de Prueba");
        leagueCreateDto.setDescription("Una descripción de prueba");
        leagueCreateDto.setTeamSize(5);
        leagueCreateDto.setPrivate(true);

        targetUser = new User();
        targetUser.setId(2L);
        targetUser.setUsername("targetuser");
    }

    @Test
    void createLeague_whenUserExists_shouldCreateLeagueAndAssignAdminRole() {
        long MOCKED_LEAGUE_ID = 10L;
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(leagueRepository.save(any(League.class))).thenAnswer(invocation -> {
            League leagueToSave = invocation.getArgument(0);
            leagueToSave.setId(MOCKED_LEAGUE_ID);
            return leagueToSave;
        });
        League leagueWithId = new League();
        leagueWithId.setId(MOCKED_LEAGUE_ID);
        leagueWithId.setTeamSize(5);
        when(leagueRepository.findById(MOCKED_LEAGUE_ID)).thenReturn(Optional.of(leagueWithId));
        Player placeholderPlayer = new Player();
        placeholderPlayer.setId(999L);
        placeholderPlayer.setName("Jugador Vacío");
        placeholderPlayer.setPlaceholder(true);
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(placeholderPlayer));
        Player playerInLeague = new Player();
        playerInLeague.setId(101L);
        playerInLeague.setName("Jugador de Prueba");
        when(playerRepository.findByLeagueIdAndIsPlaceholderFalse(MOCKED_LEAGUE_ID)).thenReturn(java.util.Collections.singletonList(playerInLeague));

        LeagueResponseDto result = leagueService.createLeague(leagueCreateDto, testUser.getId());

        assertNotNull(result);
        assertEquals(MOCKED_LEAGUE_ID, result.getId());
        assertEquals(leagueCreateDto.getName(), result.getName());
        assertEquals(1, result.getAdmins().size());

        ArgumentCaptor<UserLeagueRole> roleCaptor = ArgumentCaptor.forClass(UserLeagueRole.class);
        verify(userLeagueRoleRepository, times(1)).save(roleCaptor.capture());
        UserLeagueRole savedRole = roleCaptor.getValue();

        assertEquals(testUser.getId(), savedRole.getUser().getId());
        assertEquals(MOCKED_LEAGUE_ID, savedRole.getLeague().getId());
        assertEquals(LeagueRole.ADMIN, savedRole.getRole());
        verify(rosterPlayerRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createLeague_whenUserDoesNotExist_shouldThrowNotFoundException() {
        long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> leagueService.createLeague(leagueCreateDto, nonExistentUserId));
        verify(leagueRepository, never()).save(any(League.class));
        verify(userLeagueRoleRepository, never()).save(any(UserLeagueRole.class));
    }

    @Test
    void joinLeague_whenCodeIsValidAndLeagueIsPublic_shouldAddUserAsParticipant() {
        String joinCode = "VALIDCODE";
        long leagueId = 20L;
        League publicLeague = new League();
        publicLeague.setId(leagueId);
        publicLeague.setJoinCode(joinCode);
        publicLeague.setPrivate(false);
        publicLeague.setTeamSize(5);

        when(leagueRepository.findByJoinCode(joinCode)).thenReturn(Optional.of(publicLeague));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(publicLeague.getId(), testUser.getId())).thenReturn(false);
        when(playerRepository.findByIsPlaceholderTrue()).thenReturn(Optional.of(new Player()));
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(publicLeague));

        LeagueResponseDto result = leagueService.joinLeague(joinCode, testUser.getId());

        assertNotNull(result);
        assertEquals(publicLeague.getId(), result.getId());
        ArgumentCaptor<UserLeagueRole> roleCaptor = ArgumentCaptor.forClass(UserLeagueRole.class);
        verify(userLeagueRoleRepository, times(1)).save(roleCaptor.capture());
        UserLeagueRole savedRole = roleCaptor.getValue();
        assertEquals(testUser.getId(), savedRole.getUser().getId());
        assertEquals(publicLeague.getId(), savedRole.getLeague().getId());
        assertEquals(LeagueRole.PARTICIPANT, savedRole.getRole());
        verify(rosterPlayerRepository, times(1)).saveAll(anyList());
    }

    @Test
    void joinLeague_whenJoinCodeIsInvalid_shouldThrowNotFoundException() {
        String invalidCode = "INVALIDCODE";
        when(leagueRepository.findByJoinCode(invalidCode)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> leagueService.joinLeague(invalidCode, testUser.getId()));
        verify(userLeagueRoleRepository, never()).save(any(UserLeagueRole.class));
    }

    @Test
    void joinLeague_whenLeagueIsPrivate_shouldThrowForbiddenException() {
        String joinCode = "PRIVATECODE";
        League privateLeague = new League();
        privateLeague.setId(21L);
        privateLeague.setJoinCode(joinCode);
        privateLeague.setPrivate(true);
        when(leagueRepository.findByJoinCode(joinCode)).thenReturn(Optional.of(privateLeague));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.joinLeague(joinCode, testUser.getId()));
        assertEquals(403, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("La liga es privada"));
        verify(userLeagueRoleRepository, never()).save(any(UserLeagueRole.class));
    }

    @Test
    void joinLeague_whenUserIsAlreadyParticipant_shouldThrowConflictException() {
        String joinCode = "EXISTINGUSERCODE";
        League publicLeague = new League();
        publicLeague.setId(22L);
        publicLeague.setJoinCode(joinCode);
        publicLeague.setPrivate(false);
        when(leagueRepository.findByJoinCode(joinCode)).thenReturn(Optional.of(publicLeague));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(publicLeague.getId(), testUser.getId())).thenReturn(true);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.joinLeague(joinCode, testUser.getId()));
        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("El usuario ya es un participante"));
        verify(userLeagueRoleRepository, never()).save(any(UserLeagueRole.class));
    }

    @Test
    void changeUserRole_whenTargetUserExists_shouldChangeRole() {
        long leagueId = 30L;
        UserLeagueRole originalRole = new UserLeagueRole(targetUser, new League(), LeagueRole.PARTICIPANT);
        when(userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUser.getId())).thenReturn(Optional.of(originalRole));
        leagueService.changeUserRole(leagueId, testUser.getId(), targetUser.getId(), LeagueRole.ADMIN);
        ArgumentCaptor<UserLeagueRole> roleCaptor = ArgumentCaptor.forClass(UserLeagueRole.class);
        verify(userLeagueRoleRepository, times(1)).save(roleCaptor.capture());
        assertEquals(LeagueRole.ADMIN, roleCaptor.getValue().getRole());
    }

    @Test
    void changeUserRole_whenAdminTriesToChangeOwnRole_shouldThrowBadRequestException() {
        long leagueId = 31L;
        long adminUserId = testUser.getId();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.changeUserRole(leagueId, adminUserId, adminUserId, LeagueRole.PARTICIPANT));
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("No puedes cambiar tu propio rol"));
        verify(userLeagueRoleRepository, never()).save(any());
    }

    @Test
    void changeUserRole_whenDemotingLastAdmin_shouldThrowBadRequestException() {
        long leagueId = 32L;
        UserLeagueRole lastAdminRole = new UserLeagueRole(targetUser, new League(), LeagueRole.ADMIN);
        when(userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUser.getId())).thenReturn(Optional.of(lastAdminRole));
        when(userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN)).thenReturn(1L);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.changeUserRole(leagueId, testUser.getId(), targetUser.getId(), LeagueRole.PARTICIPANT));
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("No puedes degradar al único administrador"));
        verify(userLeagueRoleRepository, never()).save(any());
    }

    @Test
    void leaveLeague_whenUserIsParticipant_shouldDeleteRole() {
        long leagueId = 40L;
        UserLeagueRole participantRole = new UserLeagueRole(testUser, new League(), LeagueRole.PARTICIPANT);
        when(userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(Optional.of(participantRole));
        leagueService.leaveLeague(leagueId, testUser.getId());
        verify(userLeagueRoleRepository, times(1)).delete(participantRole);
    }

    @Test
    void leaveLeague_whenLastAdminTriesToLeave_shouldThrowBadRequestException() {
        long leagueId = 41L;
        UserLeagueRole adminRole = new UserLeagueRole(testUser, new League(), LeagueRole.ADMIN);
        when(userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(Optional.of(adminRole));
        when(userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN)).thenReturn(1L);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.leaveLeague(leagueId, testUser.getId()));
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("siendo el único administrador"));
        verify(userLeagueRoleRepository, never()).delete(any());
    }

    @Test
    void updateLeagueTeamSize_whenUserIsAdmin_shouldUpdateTeamSize() {
        long leagueId = 50L;
        League league = new League();
        league.setId(leagueId);
        league.setTeamSize(5);
        when(userLeagueRoleRepository.findAllByLeagueId(leagueId)).thenReturn(java.util.Collections.singletonList(new UserLeagueRole(testUser, league, LeagueRole.ADMIN)));
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        LeagueTeamSizeUpdateDto updateDto = new LeagueTeamSizeUpdateDto();
        updateDto.setTeamSize(7);
        leagueService.updateLeagueTeamSize(leagueId, updateDto, testUser.getId());
        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);
        verify(leagueRepository, times(1)).save(leagueCaptor.capture());
        assertEquals(7, leagueCaptor.getValue().getTeamSize());
    }

    @Test
    void updateLeagueTeamSize_whenUserIsNotAdmin_shouldThrowForbiddenException() {
        long leagueId = 51L;
        League league = new League();
        league.setId(leagueId);
        when(userLeagueRoleRepository.findAllByLeagueId(leagueId)).thenReturn(java.util.Collections.singletonList(new UserLeagueRole(testUser, league, LeagueRole.PARTICIPANT)));
        LeagueTeamSizeUpdateDto updateDto = new LeagueTeamSizeUpdateDto();
        updateDto.setTeamSize(7);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.updateLeagueTeamSize(leagueId, updateDto, testUser.getId()));
        assertEquals(403, exception.getStatusCode().value());
        verify(leagueRepository, never()).save(any());
    }

    @Test
    void sendJoinRequest_whenLeagueIsPrivateAndUserIsNotMember_shouldSaveRequest() {
        long leagueId = 60L;
        League privateLeague = new League();
        privateLeague.setId(leagueId);
        privateLeague.setPrivate(true);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(privateLeague));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(false);
        when(leagueJoinRequestRepository.findByUserAndLeagueAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        leagueService.sendJoinRequest(leagueId, testUser.getId());
        ArgumentCaptor<LeagueJoinRequest> requestCaptor = ArgumentCaptor.forClass(LeagueJoinRequest.class);
        verify(leagueJoinRequestRepository, times(1)).save(requestCaptor.capture());
        assertEquals(testUser.getId(), requestCaptor.getValue().getUser().getId());
        assertEquals(leagueId, requestCaptor.getValue().getLeague().getId());
        assertEquals(com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus.PENDING, requestCaptor.getValue().getStatus());
    }

    @Test
    void sendJoinRequest_whenLeagueIsNotPrivate_shouldThrowBadRequestException() {
        long leagueId = 61L;
        League publicLeague = new League();
        publicLeague.setId(leagueId);
        publicLeague.setPrivate(false);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(publicLeague));
        assertThrows(ResponseStatusException.class, () -> leagueService.sendJoinRequest(leagueId, testUser.getId()));
        verify(leagueJoinRequestRepository, never()).save(any());
    }

    @Test
    void acceptJoinRequest_whenRequestExists_shouldAddUserAndUpdateRequestStatus() {
        long requestId = 70L;
        League league = new League();
        league.setId(1L);
        LeagueJoinRequest request = new LeagueJoinRequest();
        request.setId(requestId);
        request.setUser(testUser);
        request.setLeague(league);
        request.setStatus(com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus.PENDING);
        when(leagueJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        leagueService.acceptJoinRequest(requestId);
        ArgumentCaptor<LeagueJoinRequest> requestCaptor = ArgumentCaptor.forClass(LeagueJoinRequest.class);
        verify(leagueJoinRequestRepository, times(1)).save(requestCaptor.capture());
        assertEquals(com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus.ACCEPTED, requestCaptor.getValue().getStatus());
        ArgumentCaptor<UserLeagueRole> roleCaptor = ArgumentCaptor.forClass(UserLeagueRole.class);
        verify(userLeagueRoleRepository, times(1)).save(roleCaptor.capture());
        assertEquals(LeagueRole.PARTICIPANT, roleCaptor.getValue().getRole());
        assertEquals(testUser.getId(), roleCaptor.getValue().getUser().getId());
    }

    @Test
    void rejectJoinRequest_whenRequestExists_shouldDeleteRequest() {
        long requestId = 71L;
        LeagueJoinRequest request = new LeagueJoinRequest();
        request.setId(requestId);
        when(leagueJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        leagueService.rejectJoinRequest(requestId);
        verify(leagueJoinRequestRepository, times(1)).delete(request);
    }

    @Test
    void expelUser_whenAdminExpelsParticipant_shouldDeleteRole() {
        long leagueId = 80L;
        UserLeagueRole targetUserRole = new UserLeagueRole(targetUser, new League(), LeagueRole.PARTICIPANT);
        when(userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUser.getId())).thenReturn(Optional.of(targetUserRole));
        leagueService.expelUser(leagueId, testUser.getId(), targetUser.getId());
        verify(userLeagueRoleRepository, times(1)).delete(targetUserRole);
    }

    @Test
    void expelUser_whenUserTriesToExpelSelf_shouldThrowBadRequest() {
        long leagueId = 81L;
        assertThrows(ResponseStatusException.class, () -> leagueService.expelUser(leagueId, testUser.getId(), testUser.getId()));
        verify(userLeagueRoleRepository, never()).delete(any());
    }

    @Test
    void deleteLeague_whenUserIsAdmin_shouldDeleteLeagueAndAllRelatedData() {
        long leagueId = 90L;
        League league = new League();
        league.setId(leagueId);
        when(userLeagueRoleRepository.findAllByLeagueId(leagueId)).thenReturn(java.util.Collections.singletonList(new UserLeagueRole(testUser, league, LeagueRole.ADMIN)));
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        leagueService.deleteLeague(leagueId, testUser.getId());
        verify(rosterPlayerRepository, times(1)).deleteAll(anyList());
        verify(playerRepository, times(1)).deleteAll(anyList());
        verify(userLeagueRoleRepository, times(1)).deleteAll(anyList());
        verify(leagueRepository, times(1)).delete(league);
    }

    @Test
    void deleteLeague_whenUserIsNotAdmin_shouldThrowForbiddenException() {
        long leagueId = 91L;
        League league = new League();
        league.setId(leagueId);
        when(userLeagueRoleRepository.findAllByLeagueId(leagueId)).thenReturn(java.util.Collections.singletonList(new UserLeagueRole(testUser, league, LeagueRole.PARTICIPANT)));
        assertThrows(ResponseStatusException.class, () -> leagueService.deleteLeague(leagueId, testUser.getId()));
        verify(leagueRepository, never()).delete(any());
    }

    @Test
    void getLeagueById_whenUserIsMember_shouldReturnLeagueDto() {
        long leagueId = 100L;
        League league = new League();
        league.setId(leagueId);
        league.setName("Liga de Acceso Permitido");
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(true);
        LeagueResponseDto result = leagueService.getLeagueById(leagueId, testUser.getId());
        assertNotNull(result);
        assertEquals(league.getName(), result.getName());
    }

    @Test
    void getLeagueById_whenUserIsNotMember_shouldThrowForbiddenException() {
        long leagueId = 101L;
        League league = new League();
        league.setId(leagueId);
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> leagueService.getLeagueById(leagueId, testUser.getId()));
    }

    @Test
    void getLeagueScoreboard_shouldReturnCorrectlyRankedScores() {
        long leagueId = 110L;
        Player player1 = new Player();
        player1.setId(101L);
        Player player2 = new Player();
        player2.setId(102L);
        RosterPlayer rosterPlayerUser1 = new RosterPlayer();
        rosterPlayerUser1.setPlayer(player1);
        rosterPlayerUser1.setRole(com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole.CAMPO);
        RosterPlayer rosterPlayerUser2 = new RosterPlayer();
        rosterPlayerUser2.setPlayer(player2);
        rosterPlayerUser2.setRole(com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole.CAMPO);
        PlayerMatchStats statsPlayer1 = new PlayerMatchStats();
        statsPlayer1.setTotalFieldPoints(15.0);
        PlayerMatchStats statsPlayer2 = new PlayerMatchStats();
        statsPlayer2.setTotalFieldPoints(10.0);
        when(rosterPlayerRepository.findDistinctUserIdsByLeagueId(leagueId)).thenReturn(java.util.Arrays.asList(testUser.getId(), targetUser.getId()));
        when(rosterPlayerRepository.findByUserIdAndLeagueId(testUser.getId(), leagueId)).thenReturn(java.util.Collections.singletonList(rosterPlayerUser1));
        when(rosterPlayerRepository.findByUserIdAndLeagueId(targetUser.getId(), leagueId)).thenReturn(java.util.Collections.singletonList(rosterPlayerUser2));
        when(playerMatchStatsRepository.findByPlayerId(player1.getId())).thenReturn(java.util.Collections.singletonList(statsPlayer1));
        when(playerMatchStatsRepository.findByPlayerId(player2.getId())).thenReturn(java.util.Collections.singletonList(statsPlayer2));
        java.util.List<com.fantasycolegas.fantasy_colegas_backend.dto.response.UserScoreDto> scoreboard = leagueService.getLeagueScoreboard(leagueId);
        assertEquals(2, scoreboard.size());
        assertEquals(testUser.getId(), scoreboard.get(0).getUserId());
        assertEquals(15.0, scoreboard.get(0).getTotalPoints());
        assertEquals(targetUser.getId(), scoreboard.get(1).getUserId());
        assertEquals(10.0, scoreboard.get(1).getTotalPoints());
    }

    @Test
    void updateLeague_whenLeagueExists_shouldUpdateAndReturnDto() {
        long leagueId = 120L;
        League existingLeague = new League();
        existingLeague.setId(leagueId);
        existingLeague.setName("Nombre Antiguo");
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(existingLeague));
        when(leagueRepository.save(any(League.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LeagueCreateDto updateDto = new LeagueCreateDto();
        updateDto.setName("Nombre Nuevo");
        updateDto.setDescription("Nueva descripción");
        LeagueResponseDto result = leagueService.updateLeague(leagueId, updateDto);
        assertNotNull(result);
        assertEquals("Nombre Nuevo", result.getName());
        assertEquals("Nueva descripción", result.getDescription());
        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);
        verify(leagueRepository).save(leagueCaptor.capture());
        assertEquals("Nombre Nuevo", leagueCaptor.getValue().getName());
    }

    @Test
    void getRosterByTeamId_whenUserIsNotMember_shouldThrowAccessDeniedException() {
        long leagueId = 130L;
        long teamId = targetUser.getId();
        User nonMemberUser = new User();
        nonMemberUser.setId(3L);
        nonMemberUser.setUsername("nonmember");
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(new League()));
        when(userRepository.findByUsername(nonMemberUser.getUsername())).thenReturn(Optional.of(nonMemberUser));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, nonMemberUser.getId())).thenReturn(false);
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> leagueService.getRosterByTeamId(leagueId, teamId, nonMemberUser.getUsername()));
    }

    @Test
    void sendJoinRequest_whenRequestIsAlreadyPending_shouldThrowConflictException() {
        long leagueId = 62L;
        League privateLeague = new League();
        privateLeague.setId(leagueId);
        privateLeague.setPrivate(true);

        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(privateLeague));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(false);
        when(leagueJoinRequestRepository.findByUserAndLeagueAndStatus(testUser, privateLeague, com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus.PENDING)).thenReturn(Optional.of(new LeagueJoinRequest()));

        assertThrows(ResponseStatusException.class, () -> {
            leagueService.sendJoinRequest(leagueId, testUser.getId());
        });

        verify(leagueJoinRequestRepository, never()).save(any());
    }

    @Test
    void expelUser_whenAdminExpelsAnotherAdminWithMoreAdminsRemaining_shouldSucceed() {
        long leagueId = 82L;
        UserLeagueRole targetUserRole = new UserLeagueRole(targetUser, new League(), LeagueRole.ADMIN);

        when(userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUser.getId())).thenReturn(Optional.of(targetUserRole));
        when(userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN)).thenReturn(2L);

        leagueService.expelUser(leagueId, testUser.getId(), targetUser.getId());

        verify(userLeagueRoleRepository, times(1)).delete(targetUserRole);
    }

    @Test
    void getRosterByTeamId_whenUserIsMember_shouldReturnRoster() {
        long leagueId = 131L;
        long teamId = targetUser.getId();
        League league = new League();
        league.setId(leagueId);

        RosterPlayer rosterPlayer = new RosterPlayer();
        rosterPlayer.setUser(targetUser);
        rosterPlayer.setLeague(league);
        rosterPlayer.setPlayer(new Player());

        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, testUser.getId())).thenReturn(true);
        when(rosterPlayerRepository.findByLeagueAndUser_Id(league, teamId)).thenReturn(java.util.Collections.singletonList(rosterPlayer));

        com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterResponseDto result = leagueService.getRosterByTeamId(leagueId, teamId, testUser.getUsername());

        assertNotNull(result);
        assertEquals(teamId, result.getUserId());
    }

    @Test
    void getUserPointsInLeague_shouldReturnCorrectTotalPoints() {
        long leagueId = 140L;
        Player player1 = new Player();
        player1.setId(101L);
        Player player2 = new Player();
        player2.setId(102L);

        RosterPlayer rosterPlayer1 = new RosterPlayer();
        rosterPlayer1.setPlayer(player1);
        rosterPlayer1.setRole(com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole.CAMPO);

        RosterPlayer rosterPlayer2 = new RosterPlayer();
        rosterPlayer2.setPlayer(player2);
        rosterPlayer2.setRole(com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole.PORTERO);

        PlayerMatchStats stats1 = new PlayerMatchStats();
        stats1.setTotalFieldPoints(20.0);

        PlayerMatchStats stats2 = new PlayerMatchStats();
        stats2.setTotalGoalkeeperPoints(5.0);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(rosterPlayerRepository.findByUserIdAndLeagueId(testUser.getId(), leagueId)).thenReturn(java.util.Arrays.asList(rosterPlayer1, rosterPlayer2));
        when(playerMatchStatsRepository.findByPlayerId(player1.getId())).thenReturn(java.util.Collections.singletonList(stats1));
        when(playerMatchStatsRepository.findByPlayerId(player2.getId())).thenReturn(java.util.Collections.singletonList(stats2));

        com.fantasycolegas.fantasy_colegas_backend.dto.response.UserScoreDto result = leagueService.getUserPointsInLeague(leagueId, testUser.getId());

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(25.0, result.getTotalPoints());
    }

    @Test
    void getLeaguesByUserId_whenUserExists_shouldReturnLeagues() {
        League league1 = new League();
        league1.setId(101L);
        league1.setName("Liga 1");

        League league2 = new League();
        league2.setId(102L);
        league2.setName("Liga 2");

        UserLeagueRole role1 = new UserLeagueRole(testUser, league1, LeagueRole.PARTICIPANT);
        UserLeagueRole role2 = new UserLeagueRole(testUser, league2, LeagueRole.ADMIN);

        testUser.setLeagueRoles(Set.of(role1, role2));

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        List<LeagueResponseDto> result = leagueService.getLeaguesByUserId(testUser.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(l -> l.getName().equals("Liga 1")));
        assertTrue(result.stream().anyMatch(l -> l.getName().equals("Liga 2")));
    }

    @Test
    void getLeaguesByUserId_whenUserDoesNotExist_shouldThrowNotFoundException() {
        long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> leagueService.getLeaguesByUserId(nonExistentUserId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getPublicLeagues_shouldReturnOnlyPublicLeagues() {
        League publicLeague1 = new League();
        publicLeague1.setId(1L);
        publicLeague1.setName("Liga Pública 1");
        publicLeague1.setPrivate(false);

        League publicLeague2 = new League();
        publicLeague2.setId(2L);
        publicLeague2.setName("Liga Pública 2");
        publicLeague2.setPrivate(false);

        when(leagueRepository.findAllByIsPrivateFalse()).thenReturn(List.of(publicLeague1, publicLeague2));

        List<LeagueResponseDto> result = leagueService.getPublicLeagues();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(leagueRepository, times(1)).findAllByIsPrivateFalse();
    }

    @Test
    void searchLeaguesByName_whenNameIsValid_shouldReturnMatchingLeagues() {
        String searchTerm = "profesional";
        League league1 = new League();
        league1.setId(1L);
        league1.setName("Liga Profesional de Test");

        when(leagueRepository.findByNameContainingIgnoreCase(searchTerm)).thenReturn(List.of(league1));

        List<LeagueResponseDto> result = leagueService.searchLeaguesByName(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Liga Profesional de Test", result.get(0).getName());
        verify(leagueRepository, times(1)).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void searchLeaguesByName_whenNameIsEmpty_shouldReturnEmptyList() {
        List<LeagueResponseDto> result = leagueService.searchLeaguesByName("  ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(leagueRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void getLeagueByJoinCode_whenCodeExists_shouldReturnLeague() {
        String validCode = "VALID123";
        League league = new League();
        league.setId(1L);
        league.setName("Liga por Código");
        league.setJoinCode(validCode);

        when(leagueRepository.findByJoinCode(validCode)).thenReturn(Optional.of(league));

        LeagueResponseDto result = leagueService.getLeagueByJoinCode(validCode);

        assertNotNull(result);
        assertEquals(league.getId(), result.getId());
        assertEquals(league.getName(), result.getName());
        verify(leagueRepository, times(1)).findByJoinCode(validCode);
    }

    @Test
    void getLeagueByJoinCode_whenCodeDoesNotExist_shouldThrowNotFoundException() {
        String invalidCode = "INVALID456";
        when(leagueRepository.findByJoinCode(invalidCode)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> leagueService.getLeagueByJoinCode(invalidCode));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(leagueRepository, times(1)).findByJoinCode(invalidCode);
    }
}