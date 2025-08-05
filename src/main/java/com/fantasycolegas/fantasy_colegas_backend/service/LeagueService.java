package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueJoinRequestRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserLeagueRoleRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final UserLeagueRoleRepository userLeagueRoleRepository;
    private final LeagueJoinRequestRepository leagueJoinRequestRepository;

    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository, UserLeagueRoleRepository userLeagueRoleRepository, LeagueJoinRequestRepository leagueJoinRequestRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.userLeagueRoleRepository = userLeagueRoleRepository;
        this.leagueJoinRequestRepository = leagueJoinRequestRepository;
    }

    @Transactional
    public boolean checkIfUserIsMember(Long leagueId, Long userId) {
        return userLeagueRoleRepository.existsByLeagueIdAndUserId(leagueId, userId);
    }


    @Transactional
    public LeagueResponseDto createLeague(League newLeague, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // El número de jugadores inicial es 1 (el creador)
        newLeague.setNumberOfPlayers(1);
        League savedLeague = leagueRepository.save(newLeague);

        // Asignar al creador el rol de ADMIN.
        // No creamos un rol de PARTICIPANT, ya que un ADMIN es también un PARTICIPANT por definición.
        UserLeagueRole adminRole = new UserLeagueRole(creator, savedLeague, LeagueRole.ADMIN);
        userLeagueRoleRepository.save(adminRole);

        // IMPORTANTE: Recargar la liga para que su colección de roles esté actualizada
        League reloadedLeague = leagueRepository.findById(savedLeague.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga creada pero no encontrada"));

        return mapToLeagueResponseDto(reloadedLeague);
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

        // Verificamos si el usuario ya tiene un rol en la liga
        if (userLeagueRoleRepository.existsByLeagueIdAndUserId(league.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es un participante de esta liga");
        }

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);

        league.setNumberOfPlayers(league.getNumberOfPlayers() + 1);
        League updatedLeague = leagueRepository.save(league);

        return mapToLeagueResponseDto(updatedLeague);
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

        // Verificamos si ya existe una solicitud pendiente
        if (leagueJoinRequestRepository.findByUserAndLeagueAndStatus(user, league, RequestStatus.PENDING).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud de unión pendiente para esta liga");
        }

        // Creamos y guardamos la nueva solicitud
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

        // Marcar la solicitud como aceptada
        request.setStatus(RequestStatus.ACCEPTED);
        leagueJoinRequestRepository.save(request);

        // Añadir al usuario como participante
        User user = request.getUser();
        League league = request.getLeague();

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);

        // Actualizar el número de jugadores
        league.setNumberOfPlayers(league.getNumberOfPlayers() + 1);
        leagueRepository.save(league);
    }

    @Transactional
    public void rejectJoinRequest(Long requestId) {
        LeagueJoinRequest request = leagueJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        // Eliminar la solicitud
        leagueJoinRequestRepository.delete(request);
    }

    @Transactional
    public LeagueResponseDto getLeagueById(Long id, Long userId) {
        // 1. Verificamos que la liga existe
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        // 2. Verificamos que el usuario es miembro de la liga
        if (!checkIfUserIsMember(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres miembro de esta liga");
        }

        // 3. Si es miembro, devolvemos los detalles
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

    @Transactional
    public boolean checkIfUserIsAdmin(Long leagueId, Long userId) {
        // Obtenemos el rol del usuario en la liga
        return userLeagueRoleRepository.findByLeagueId(leagueId).stream()
                .anyMatch(ulr -> ulr.getUser().getId().equals(userId) && ulr.getRole() == LeagueRole.ADMIN);
    }

    private LeagueResponseDto mapToLeagueResponseDto(League league) {
        Set<UserLeagueRole> userRoles = new HashSet<>(league.getUserRoles());

        List<UserResponseDto> adminsDto = userRoles.stream()
                .filter(ulr -> ulr.getRole() == LeagueRole.ADMIN)
                .map(ulr -> mapToUserResponseDto(ulr.getUser()))
                .collect(Collectors.toList());

        // La lista de participantes incluye a todos, tanto admins como participants
        List<UserResponseDto> participantsDto = userRoles.stream()
                .map(ulr -> mapToUserResponseDto(ulr.getUser()))
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
                participantsDto
        );
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername());
    }
}