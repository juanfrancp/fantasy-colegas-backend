// Archivo: LeagueService.java
package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueTeamSizeUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.*;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus;
import com.fantasycolegas.fantasy_colegas_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final UserLeagueRoleRepository userLeagueRoleRepository;
    private final LeagueJoinRequestRepository leagueJoinRequestRepository;
    private final PlayerRepository playerRepository;
    private final RosterPlayerRepository rosterPlayerRepository;
    private PlayerMatchStatsRepository playerMatchStatsRepository;

    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository, UserLeagueRoleRepository userLeagueRoleRepository, LeagueJoinRequestRepository leagueJoinRequestRepository, PlayerRepository playerRepository, RosterPlayerRepository rosterPlayerRepository, PlayerMatchStatsRepository playerMatchStatsRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.userLeagueRoleRepository = userLeagueRoleRepository;
        this.leagueJoinRequestRepository = leagueJoinRequestRepository;
        this.playerRepository = playerRepository;
        this.rosterPlayerRepository = rosterPlayerRepository;
        this.playerMatchStatsRepository = playerMatchStatsRepository;
    }


    public List<UserScoreDto> getLeagueScoreboard(Long leagueId) {
        List<Long> userIds = rosterPlayerRepository.findDistinctUserIdsByLeagueId(leagueId);

        List<UserScoreDto> scoreboard = new ArrayList<>();
        for (Long userId : userIds) {
            double totalPoints = calculateUserPoints(leagueId, userId);
            UserScoreDto userScore = new UserScoreDto(userId, totalPoints);
            scoreboard.add(userScore);
        }

        scoreboard.sort(Comparator.comparingDouble(UserScoreDto::getTotalPoints).reversed());
        return scoreboard;
    }

    public UserScoreDto getUserPointsInLeague(Long leagueId, Long userId) {
        double totalPoints = calculateUserPoints(leagueId, userId);
        return new UserScoreDto(userId, totalPoints);
    }

    private double calculateUserPoints(Long leagueId, Long userId) {
        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);

        double totalUserPoints = 0;
        for (RosterPlayer rosterPlayer : rosterPlayers) {
            List<PlayerMatchStats> stats = playerMatchStatsRepository.findByPlayerId(rosterPlayer.getPlayer().getId());
            for (PlayerMatchStats stat : stats) {
                if (rosterPlayer.getRole() == PlayerTeamRole.CAMPO) {
                    totalUserPoints += stat.getTotalFieldPoints();
                } else if (rosterPlayer.getRole() == PlayerTeamRole.PORTERO) {
                    totalUserPoints += stat.getTotalGoalkeeperPoints();
                }
            }
        }
        return totalUserPoints;
    }

    public RosterResponseDto getRosterByTeamId(Long leagueId, Long teamId, String requestingUsername) {
        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new IllegalArgumentException("League not found with ID: " + leagueId));
        User requestingUser = userRepository.findByUsername(requestingUsername).orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        boolean isRequestingUserInLeague = userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, requestingUser.getId());
        if (!isRequestingUserInLeague) {
            throw new AccessDeniedException("The requesting user is not a member of this league.");
        }
        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findByLeagueAndUser_Id(league, teamId);
        if (rosterPlayers.isEmpty()) {
            throw new IllegalArgumentException("Roster not found for user ID " + teamId + " in league ID: " + leagueId);
        }

        User user = rosterPlayers.get(0).getUser();

        List<RosterPlayerResponseDto> playerDTOs = rosterPlayers.stream().map(rp -> new RosterPlayerResponseDto(rp.getPlayer().getId(), rp.getPlayer().getName(), rp.getRole(), rp.getPlayer().getImage(), rp.getPlayer().getTotalPoints())).collect(Collectors.toList());

        RosterResponseDto rosterResponseDto = new RosterResponseDto();
        rosterResponseDto.setLeagueId(league.getId());
        rosterResponseDto.setLeagueName(league.getName());
        rosterResponseDto.setUserId(user.getId());
        rosterResponseDto.setUsername(user.getUsername());
        rosterResponseDto.setPlayers(playerDTOs);

        return rosterResponseDto;
    }

    @Transactional
    public LeagueResponseDto updateLeagueTeamSize(Long leagueId, LeagueTeamSizeUpdateDto teamSizeUpdateDto, Long userId) {
        if (!checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores pueden cambiar el tamaño del equipo.");
        }

        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        league.setTeamSize(teamSizeUpdateDto.getTeamSize());

        leagueRepository.save(league);
        return mapToLeagueResponseDto(league);
    }

    @Transactional
    public void changeUserRole(Long leagueId, Long adminUserId, Long targetUserId, LeagueRole newRole) {
        if (adminUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.");
        }

        UserLeagueRole targetUserRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUserId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no es miembro de esta liga."));

        if (targetUserRole.getRole() == LeagueRole.ADMIN && newRole == LeagueRole.PARTICIPANT) {
            long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
            if (adminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes degradar al único administrador de la liga.");
            }
        }

        targetUserRole.setRole(newRole);
        userLeagueRoleRepository.save(targetUserRole);
    }

    @Transactional
    public LeagueResponseDto createLeague(LeagueCreateDto leagueCreateDto, Long userId) {
        User creator = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        League newLeague = new League();
        newLeague.setName(leagueCreateDto.getName());
        newLeague.setDescription(leagueCreateDto.getDescription());
        newLeague.setImage(leagueCreateDto.getImage());
        newLeague.setPrivate(leagueCreateDto.isPrivate());
        newLeague.setTeamSize(leagueCreateDto.getTeamSize());

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder joinCodeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            joinCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        newLeague.setJoinCode(joinCodeBuilder.toString());

        newLeague.setNumberOfPlayers(1);

        League savedLeague = leagueRepository.save(newLeague);

        UserLeagueRole adminRole = new UserLeagueRole(creator, savedLeague, LeagueRole.ADMIN);
        userLeagueRoleRepository.save(adminRole);


        createRandomRosterForUser(newLeague.getId(), creator.getId());

        return mapToLeagueResponseDto(savedLeague);
    }

    @Transactional
    public LeagueResponseDto joinLeague(String joinCode, Long userId) {
        League league = leagueRepository.findByJoinCode(joinCode).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código de invitación inválido o la liga no existe"));

        if (league.isPrivate()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La liga es privada y requiere una solicitud de unión");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (userLeagueRoleRepository.existsByLeagueIdAndUserId(league.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es un participante de esta liga");
        }

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);

        createRandomRosterForUser(league.getId(), userId);

        return mapToLeagueResponseDto(league);
    }

    @Transactional
    public void createRandomRosterForUser(Long leagueId, Long userId) {
        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        List<Player> allPlayersInLeague = playerRepository.findByLeagueIdAndIsPlaceholderFalse(leagueId);

        Player placeholderPlayer = playerRepository.findByIsPlaceholderTrue().orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Jugador vacío no encontrado."));

        Collections.shuffle(allPlayersInLeague);

        List<Player> teamPlayers = new ArrayList<>();
        int teamSize = league.getTeamSize();

        for (int i = 0; i < Math.min(teamSize, allPlayersInLeague.size()); i++) {
            teamPlayers.add(allPlayersInLeague.get(i));
        }

        while (teamPlayers.size() < teamSize) {
            teamPlayers.add(placeholderPlayer);
        }

        rosterPlayerRepository.deleteByUserIdAndLeagueId(userId, leagueId);

        List<RosterPlayer> roster = new ArrayList<>();

        Player portero = teamPlayers.get(0);
        RosterPlayer porteroRoster = new RosterPlayer();
        porteroRoster.setUser(user);
        porteroRoster.setLeague(league);
        porteroRoster.setPlayer(portero);
        porteroRoster.setRole(PlayerTeamRole.PORTERO);
        roster.add(porteroRoster);

        for (int i = 1; i < teamPlayers.size(); i++) {
            Player campo = teamPlayers.get(i);
            RosterPlayer campoRoster = new RosterPlayer();
            campoRoster.setUser(user);
            campoRoster.setLeague(league);
            campoRoster.setPlayer(campo);
            campoRoster.setRole(PlayerTeamRole.CAMPO);
            roster.add(campoRoster);
        }

        rosterPlayerRepository.saveAll(roster);
    }

    @Transactional
    public void sendJoinRequest(Long leagueId, Long userId) {
        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        if (!league.isPrivate()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta liga no es privada, no necesita solicitud");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (userLeagueRoleRepository.existsByLeagueIdAndUserId(league.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya eres un miembro de esta liga.");
        }

        if (leagueJoinRequestRepository.findByUserAndLeagueAndStatus(user, league, RequestStatus.PENDING).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud de unión pendiente para esta liga");
        }

        LeagueJoinRequest request = new LeagueJoinRequest();
        request.setUser(user);
        request.setLeague(league);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);
        leagueJoinRequestRepository.save(request);
    }

    @Transactional
    public List<LeagueJoinRequest> getPendingJoinRequests(Long leagueId) {
        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        return leagueJoinRequestRepository.findByLeagueAndStatus(league, RequestStatus.PENDING);
    }

    @Transactional
    public void acceptJoinRequest(Long requestId) {
        LeagueJoinRequest request = leagueJoinRequestRepository.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        request.setStatus(RequestStatus.ACCEPTED);
        leagueJoinRequestRepository.save(request);

        User user = request.getUser();
        League league = request.getLeague();

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);
    }

    @Transactional
    public void rejectJoinRequest(Long requestId) {
        LeagueJoinRequest request = leagueJoinRequestRepository.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        leagueJoinRequestRepository.delete(request);
    }

    @Transactional
    public LeagueResponseDto getLeagueById(Long id, Long userId) {
        League league = leagueRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        if (!checkIfUserIsMember(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres miembro de esta liga");
        }

        return mapToLeagueResponseDto(league);
    }

    @Transactional
    public LeagueResponseDto updateLeague(Long id, LeagueCreateDto leagueCreateDto) {
        League existingLeague = leagueRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        existingLeague.setName(leagueCreateDto.getName());
        existingLeague.setDescription(leagueCreateDto.getDescription());
        existingLeague.setImage(leagueCreateDto.getImage());
        existingLeague.setPrivate(leagueCreateDto.isPrivate());
        existingLeague.setNumberOfPlayers(leagueCreateDto.getNumberOfPlayers());
        existingLeague.setTeamSize(leagueCreateDto.getTeamSize());

        League updatedLeague = leagueRepository.save(existingLeague);
        return mapToLeagueResponseDto(updatedLeague);
    }

    @Transactional
    public void deleteLeague(Long leagueId, Long userId) {
        if (!checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador para eliminar esta liga.");
        }

        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findAllByLeagueId(leagueId);
        rosterPlayerRepository.deleteAll(rosterPlayers);

        List<Player> players = playerRepository.findAllByLeagueId(leagueId);
        playerRepository.deleteAll(players);

        List<UserLeagueRole> userLeagueRoles = userLeagueRoleRepository.findAllByLeagueId(leagueId);
        userLeagueRoleRepository.deleteAll(userLeagueRoles);

        leagueRepository.delete(league);
    }

    @Transactional
    public void leaveLeague(Long leagueId, Long userId) {
        UserLeagueRole userRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es miembro de esta liga."));

        long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
        if (adminCount == 1 && userRole.getRole() == LeagueRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes abandonar la liga siendo el único administrador.");
        }

        userLeagueRoleRepository.delete(userRole);
    }

    @Transactional
    public void expelUser(Long leagueId, Long adminUserId, Long targetUserId) {
        if (adminUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes expulsarte a ti mismo de la liga.");
        }

        UserLeagueRole userRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUserId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no es miembro de esta liga."));

        long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
        if (adminCount == 1 && userRole.getRole() == LeagueRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes expulsar al único administrador de la liga.");
        }

        userLeagueRoleRepository.delete(userRole);
    }

    @Transactional
    public boolean checkIfUserIsAdmin(Long leagueId, Long userId) {
        return userLeagueRoleRepository.findAllByLeagueId(leagueId).stream().anyMatch(ulr -> ulr.getUser().getId().equals(userId) && ulr.getRole() == LeagueRole.ADMIN);
    }

    @Transactional
    public boolean checkIfUserIsMember(Long leagueId, Long userId) {
        return userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, userId);
    }

    private LeagueResponseDto mapToLeagueResponseDto(League league) {
        Set<UserLeagueRole> userRoles = new HashSet<>(league.getUserRoles());

        List<UserResponseDto> adminsDto = userRoles.stream().filter(ulr -> ulr.getRole() == LeagueRole.ADMIN).map(ulr -> mapToUserResponseDto(ulr.getUser())).collect(Collectors.toList());

        List<UserResponseDto> participantsDto = userRoles.stream().map(ulr -> mapToUserResponseDto(ulr.getUser())).collect(Collectors.toList());

        List<PlayerResponseDto> players = league.getPlayers().stream().map(this::mapToPlayerResponseDto).collect(Collectors.toList());

        return new LeagueResponseDto(league.getId(), league.getName(), league.getDescription(), league.getImage(), league.isPrivate(), league.getJoinCode(), participantsDto.size(), adminsDto, participantsDto, league.getTeamSize(), players);
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername());
    }

    private PlayerResponseDto mapToPlayerResponseDto(Player player) {
        return new PlayerResponseDto(player.getId(), player.getName(), player.getImage(), player.getTotalPoints());
    }

    public boolean isUserParticipant(Long leagueId, Long userId) {
        return userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, userId);
    }
}