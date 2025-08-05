// Archivo: LeagueService.java
package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueTeamSizeUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserLeagueRoleRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueJoinRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final UserLeagueRoleRepository userLeagueRoleRepository;
    private final LeagueJoinRequestRepository leagueJoinRequestRepository;

    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository,
                         UserLeagueRoleRepository userLeagueRoleRepository,
                         LeagueJoinRequestRepository leagueJoinRequestRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.userLeagueRoleRepository = userLeagueRoleRepository;
        this.leagueJoinRequestRepository = leagueJoinRequestRepository;
    }

    @Transactional
    public LeagueResponseDto updateLeagueTeamSize(Long leagueId, LeagueTeamSizeUpdateDto teamSizeUpdateDto, Long userId) {
        // 1. Verificar si el usuario es administrador
        if (!checkIfUserIsAdmin(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores pueden cambiar el tamaño del equipo.");
        }

        // 2. Obtener la liga
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        // 3. Actualizar el tamaño del equipo
        league.setTeamSize(teamSizeUpdateDto.getTeamSize());

        // 4. Guardar y retornar
        leagueRepository.save(league);
        return mapToLeagueResponseDto(league);
    }

    @Transactional
    public void changeUserRole(Long leagueId, Long adminUserId, Long targetUserId, LeagueRole newRole) {
        // 1. Validar que el administrador no está intentando cambiar su propio rol
        if (adminUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.");
        }

        // 2. Obtener el rol del usuario objetivo
        UserLeagueRole targetUserRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no es miembro de esta liga."));

        // 3. Lógica de validación importante: no se puede degradar al único administrador
        if (targetUserRole.getRole() == LeagueRole.ADMIN && newRole == LeagueRole.PARTICIPANT) {
            long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
            if (adminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes degradar al único administrador de la liga.");
            }
        }

        // 4. Actualizar el rol y guardar en la base de datos
        targetUserRole.setRole(newRole);
        userLeagueRoleRepository.save(targetUserRole);
    }

    @Transactional
    public LeagueResponseDto createLeague(LeagueCreateDto leagueCreateDto, Long userId) { // <-- CAMBIO DE PARÁMETRO
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Mapear el DTO a la entidad League
        League newLeague = new League();
        newLeague.setName(leagueCreateDto.getName());
        newLeague.setDescription(leagueCreateDto.getDescription());
        newLeague.setImage(leagueCreateDto.getImage());
        newLeague.setPrivate(leagueCreateDto.isPrivate());
        newLeague.setTeamSize(leagueCreateDto.getTeamSize()); // <-- ¡¡AQUÍ ESTÁ LA CORRECCIÓN!!

        // Lógica de negocio (movida del controlador al servicio)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder joinCodeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            joinCodeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        newLeague.setJoinCode(joinCodeBuilder.toString());

        // El número de jugadores siempre empieza en 1 (el creador)
        newLeague.setNumberOfPlayers(1);

        // Guardar la liga en la base de datos
        League savedLeague = leagueRepository.save(newLeague);

        // Asignar rol de administrador al creador y guardar
        UserLeagueRole adminRole = new UserLeagueRole(creator, savedLeague, LeagueRole.ADMIN);
        userLeagueRoleRepository.save(adminRole);

        // Mapear la liga guardada a su DTO de respuesta y retornar
        return mapToLeagueResponseDto(savedLeague);
    }

    @Transactional
    public LeagueResponseDto joinLeague(String joinCode, Long userId) {
        League league = leagueRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código de invitación inválido o la liga no existe"));

        if (league.isPrivate()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La liga es privada y requiere una solicitud de unión");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (userLeagueRoleRepository.existsByLeagueIdAndUserId(league.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es un participante de esta liga");
        }

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);

        return mapToLeagueResponseDto(league);
    }

    @Transactional
    public void sendJoinRequest(Long leagueId, Long userId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        if (!league.isPrivate()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta liga no es privada, no necesita solicitud");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

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
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        return leagueJoinRequestRepository.findByLeagueAndStatus(league, RequestStatus.PENDING);
    }

    @Transactional
    public void acceptJoinRequest(Long requestId) {
        LeagueJoinRequest request = leagueJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        request.setStatus(RequestStatus.ACCEPTED);
        leagueJoinRequestRepository.save(request);

        User user = request.getUser();
        League league = request.getLeague();

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);
    }

    @Transactional
    public void rejectJoinRequest(Long requestId) {
        LeagueJoinRequest request = leagueJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        leagueJoinRequestRepository.delete(request);
    }

    @Transactional
    public LeagueResponseDto getLeagueById(Long id, Long userId) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        if (!checkIfUserIsMember(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres miembro de esta liga");
        }

        return mapToLeagueResponseDto(league);
    }

    @Transactional
    public LeagueResponseDto updateLeague(Long id, LeagueCreateDto leagueCreateDto) {
        League existingLeague = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        existingLeague.setName(leagueCreateDto.getName());
        existingLeague.setDescription(leagueCreateDto.getDescription());
        existingLeague.setImage(leagueCreateDto.getImage());
        existingLeague.setPrivate(leagueCreateDto.isPrivate());

        League updatedLeague = leagueRepository.save(existingLeague);
        return mapToLeagueResponseDto(updatedLeague);
    }

    @Transactional
    public void deleteLeague(Long id) {
        if (!leagueRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada");
        }
        leagueRepository.deleteById(id);
    }

    // Método para salir de la liga (CORREGIDO)
    @Transactional
    public void leaveLeague(Long leagueId, Long userId) {
        UserLeagueRole userRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no es miembro de esta liga."));

        // Verificamos si el usuario que abandona es el único administrador
        long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
        if (adminCount == 1 && userRole.getRole() == LeagueRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes abandonar la liga siendo el único administrador.");
        }

        userLeagueRoleRepository.delete(userRole);
    }

    // Método para expulsar a un usuario (CORREGIDO)
    @Transactional
    public void expelUser(Long leagueId, Long adminUserId, Long targetUserId) {
        if (adminUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes expulsarte a ti mismo de la liga.");
        }

        UserLeagueRole userRole = userLeagueRoleRepository.findByLeagueIdAndUserId(leagueId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no es miembro de esta liga."));

        long adminCount = userLeagueRoleRepository.countByLeagueIdAndRole(leagueId, LeagueRole.ADMIN);
        if (adminCount == 1 && userRole.getRole() == LeagueRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes expulsar al único administrador de la liga.");
        }

        userLeagueRoleRepository.delete(userRole);
    }

    // Método para verificar si un usuario es administrador de una liga
    @Transactional
    public boolean checkIfUserIsAdmin(Long leagueId, Long userId) {
        return userLeagueRoleRepository.findByLeagueId(leagueId).stream()
                .anyMatch(ulr -> ulr.getUser().getId().equals(userId) && ulr.getRole() == LeagueRole.ADMIN);
    }

    // Método para verificar si un usuario pertenece a una liga
    @Transactional
    public boolean checkIfUserIsMember(Long leagueId, Long userId) {
        return userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, userId);
    }

    private LeagueResponseDto mapToLeagueResponseDto(League league) {
        Set<UserLeagueRole> userRoles = new HashSet<>(league.getUserRoles());

        List<UserResponseDto> adminsDto = userRoles.stream()
                .filter(ulr -> ulr.getRole() == LeagueRole.ADMIN)
                .map(ulr -> mapToUserResponseDto(ulr.getUser()))
                .collect(Collectors.toList());

        List<UserResponseDto> participantsDto = userRoles.stream()
                .map(ulr -> mapToUserResponseDto(ulr.getUser()))
                .collect(Collectors.toList());

        List<PlayerResponseDto> players = league.getPlayers().stream()
                .map(this::mapToPlayerResponseDto)
                .collect(Collectors.toList());

        return new LeagueResponseDto(
                league.getId(),
                league.getName(),
                league.getDescription(),
                league.getImage(),
                league.isPrivate(),
                league.getJoinCode(),
                participantsDto.size(),
                adminsDto,
                participantsDto,
                league.getTeamSize(),
                players
        );
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername());
    }

    private PlayerResponseDto mapToPlayerResponseDto(Player player) {
        return new PlayerResponseDto(player.getId(), player.getName(), player.getImage(), player.getTotalPoints());
    }
}