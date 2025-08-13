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

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 *
 * Servicio principal para la gestión de ligas.
 * <p>
 * Este servicio contiene la lógica de negocio para crear, unirse, modificar y eliminar ligas,
 * así como para gestionar los rosters, roles de usuario y solicitudes de unión.
 * </p>
 */
@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final UserLeagueRoleRepository userLeagueRoleRepository;
    private final LeagueJoinRequestRepository leagueJoinRequestRepository;
    private final PlayerRepository playerRepository;
    private final RosterPlayerRepository rosterPlayerRepository;
    private PlayerMatchStatsRepository playerMatchStatsRepository;

    /**
     * Constructor del servicio que inyecta las dependencias de los repositorios.
     */
    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository, UserLeagueRoleRepository userLeagueRoleRepository, LeagueJoinRequestRepository leagueJoinRequestRepository, PlayerRepository playerRepository, RosterPlayerRepository rosterPlayerRepository, PlayerMatchStatsRepository playerMatchStatsRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.userLeagueRoleRepository = userLeagueRoleRepository;
        this.leagueJoinRequestRepository = leagueJoinRequestRepository;
        this.playerRepository = playerRepository;
        this.rosterPlayerRepository = rosterPlayerRepository;
        this.playerMatchStatsRepository = playerMatchStatsRepository;
    }

    /**
     * Obtiene el ranking (scoreboard) de una liga.
     * <p>
     * Calcula los puntos de cada usuario en la liga y los devuelve ordenados de mayor a menor.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link UserScoreDto} con los puntos de cada usuario.
     */
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

    /**
     * Obtiene los puntos totales de un usuario en una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario.
     * @return Un {@link UserScoreDto} con el ID del usuario y sus puntos totales.
     */
    public UserScoreDto getUserPointsInLeague(Long leagueId, Long userId) {
        double totalPoints = calculateUserPoints(leagueId, userId);
        return new UserScoreDto(userId, totalPoints);
    }

    /**
     * Calcula los puntos totales de un usuario sumando los puntos de cada jugador en su roster.
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario.
     * @return Los puntos totales del usuario.
     */
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

    /**
     * Obtiene el roster de un equipo (usuario) en una liga.
     * <p>
     * Verifica que el usuario que realiza la petición sea miembro de la liga.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param teamId El ID del usuario cuyo roster se desea obtener.
     * @param requestingUsername El nombre de usuario que realiza la petición.
     * @return Un {@link RosterResponseDto} con los detalles del roster.
     * @throws IllegalArgumentException Si la liga, el usuario o el roster no se encuentran.
     * @throws AccessDeniedException Si el usuario que solicita no es miembro de la liga.
     */
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

    /**
     * Actualiza el tamaño del equipo de una liga.
     * <p>
     * Solo los administradores de la liga pueden realizar esta acción.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param teamSizeUpdateDto DTO con el nuevo tamaño del equipo.
     * @param userId El ID del usuario que realiza la petición.
     * @return Un {@link LeagueResponseDto} con la información actualizada de la liga.
     */
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

    /**
     * Cambia el rol de un usuario en una liga.
     * <p>
     * Valida que no se pueda degradar al único administrador de la liga.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param adminUserId El ID del usuario administrador que realiza la petición.
     * @param targetUserId El ID del usuario cuyo rol se va a cambiar.
     * @param newRole El nuevo rol a asignar.
     */
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

    /**
     * Crea una nueva liga y asigna al usuario que la crea como administrador.
     * <p>
     * También genera un código de unión y un roster aleatorio para el usuario.
     * </p>
     *
     * @param leagueCreateDto DTO con los datos para la creación de la liga.
     * @param userId El ID del usuario que crea la liga.
     * @return Un {@link LeagueResponseDto} con la información de la liga creada.
     */
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

    /**
     * Permite a un usuario unirse a una liga pública mediante un código de unión.
     *
     * @param joinCode El código de unión de la liga.
     * @param userId El ID del usuario que se quiere unir.
     * @return Un {@link LeagueResponseDto} con la información de la liga a la que se ha unido.
     */
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

    /**
     * Crea un roster aleatorio para un usuario en una liga específica.
     * <p>
     * Selecciona jugadores de la liga de forma aleatoria para formar un equipo.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario.
     */
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

    /**
     * Envía una solicitud de unión a una liga privada.
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario que solicita unirse.
     */
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

    /**
     * Obtiene todas las solicitudes de unión pendientes para una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link LeagueJoinRequest} pendientes.
     */
    @Transactional
    public List<LeagueJoinRequest> getPendingJoinRequests(Long leagueId) {
        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        return leagueJoinRequestRepository.findByLeagueAndStatus(league, RequestStatus.PENDING);
    }

    /**
     * Acepta una solicitud de unión a una liga.
     * <p>
     * Cambia el estado de la solicitud a ACEPTADA y añade al usuario a la liga como PARTICIPANT.
     * </p>
     *
     * @param requestId El ID de la solicitud.
     */
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

    /**
     * Rechaza una solicitud de unión a una liga.
     * <p>
     * La solicitud se elimina de la base de datos.
     * </p>
     *
     * @param requestId El ID de la solicitud.
     */
    @Transactional
    public void rejectJoinRequest(Long requestId) {
        LeagueJoinRequest request = leagueJoinRequestRepository.findById(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        leagueJoinRequestRepository.delete(request);
    }

    /**
     * Obtiene una liga por su ID.
     * <p>
     * Valida que el usuario que realiza la petición sea miembro de la liga.
     * </p>
     *
     * @param id El ID de la liga.
     * @param userId El ID del usuario que realiza la petición.
     * @return Un {@link LeagueResponseDto} con la información de la liga.
     */
    @Transactional
    public LeagueResponseDto getLeagueById(Long id, Long userId) {
        League league = leagueRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        if (!checkIfUserIsMember(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres miembro de esta liga");
        }

        return mapToLeagueResponseDto(league);
    }

    /**
     * Actualiza los datos de una liga.
     *
     * @param id El ID de la liga a actualizar.
     * @param leagueCreateDto DTO con los datos a actualizar.
     * @return Un {@link LeagueResponseDto} con la información actualizada de la liga.
     */
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

    /**
     * Elimina una liga por su ID.
     * <p>
     * Solo los administradores pueden eliminar la liga.
     * </p>
     *
     * @param leagueId El ID de la liga a eliminar.
     * @param userId El ID del usuario que realiza la petición.
     */
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

    /**
     * Permite a un usuario abandonar una liga.
     * <p>
     * Valida que el usuario no sea el único administrador de la liga.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario que abandona la liga.
     */
    @Transactional
    public void leaveLeague(Long leagueId, Long userId) {
        UserLeagueRole userRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es miembro de esta liga."));

        long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
        if (adminCount == 1 && userRole.getRole() == LeagueRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes abandonar la liga siendo el único administrador.");
        }

        userLeagueRoleRepository.delete(userRole);
    }

    /**
     * Expulsa a un usuario de una liga.
     * <p>
     * Solo un administrador puede expulsar a otro usuario.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param adminUserId El ID del usuario administrador que realiza la expulsión.
     * @param targetUserId El ID del usuario a expulsar.
     */
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

    /**
     * Verifica si un usuario es administrador de una liga.
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario.
     * @return {@code true} si el usuario es administrador, {@code false} en caso contrario.
     */
    @Transactional
    public boolean checkIfUserIsAdmin(Long leagueId, Long userId) {
        return userLeagueRoleRepository.findAllByLeagueId(leagueId).stream().anyMatch(ulr -> ulr.getUser().getId().equals(userId) && ulr.getRole() == LeagueRole.ADMIN);
    }

    /**
     * Verifica si un usuario es miembro (participante o administrador) de una liga.
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario.
     * @return {@code true} si el usuario es miembro, {@code false} en caso contrario.
     */
    @Transactional
    public boolean checkIfUserIsMember(Long leagueId, Long userId) {
        return userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, userId);
    }

    /**
     * Mapea una entidad {@link League} a un DTO de respuesta.
     *
     * @param league La entidad {@link League}.
     * @return El DTO de respuesta {@link LeagueResponseDto}.
     */
    private LeagueResponseDto mapToLeagueResponseDto(League league) {
        Set<UserLeagueRole> userRoles = new HashSet<>(league.getUserRoles());

        List<UserResponseDto> adminsDto = userRoles.stream().filter(ulr -> ulr.getRole() == LeagueRole.ADMIN).map(ulr -> mapToUserResponseDto(ulr.getUser())).collect(Collectors.toList());

        List<UserResponseDto> participantsDto = userRoles.stream().map(ulr -> mapToUserResponseDto(ulr.getUser())).collect(Collectors.toList());

        List<PlayerResponseDto> players = league.getPlayers().stream().map(this::mapToPlayerResponseDto).collect(Collectors.toList());

        return new LeagueResponseDto(league.getId(), league.getName(), league.getDescription(), league.getImage(), league.isPrivate(), league.getJoinCode(), participantsDto.size(), adminsDto, participantsDto, league.getTeamSize(), players);
    }

    /**
     * Mapea una entidad {@link User} a un DTO de respuesta.
     *
     * @param user La entidad {@link User}.
     * @return El DTO de respuesta {@link UserResponseDto}.
     */
    private UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername());
    }

    /**
     * Mapea una entidad {@link Player} a un DTO de respuesta.
     *
     * @param player La entidad {@link Player}.
     * @return El DTO de respuesta {@link PlayerResponseDto}.
     */
    private PlayerResponseDto mapToPlayerResponseDto(Player player) {
        return new PlayerResponseDto(player.getId(), player.getName(), player.getImage(), player.getTotalPoints());
    }

    /**
     * Verifica si un usuario es un participante (miembro) de una liga.
     *
     * @param leagueId El ID de la liga.
     * @param userId El ID del usuario.
     * @return {@code true} si el usuario es participante, {@code false} en caso contrario.
     */
    public boolean isUserParticipant(Long leagueId, Long userId) {
        return userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, userId);
    }
}