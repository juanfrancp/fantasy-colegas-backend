package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.UserLeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserLeagueRoleRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository, UserLeagueRoleRepository userLeagueRoleRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.userLeagueRoleRepository = userLeagueRoleRepository;
    }

    // Método para crear una liga (CORREGIDO Y OPTIMIZADO)
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

    // Método para unirse a una liga
    @Transactional
    public LeagueResponseDto joinLeague(String joinCode, Long userId) {
        League league = leagueRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código de invitación inválido o la liga no existe"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Verificamos si el usuario ya tiene un rol en la liga (admin o participant)
        if (userLeagueRoleRepository.existsByLeagueIdAndUserId(league.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es un participante de esta liga");
        }

        UserLeagueRole participantRole = new UserLeagueRole(user, league, LeagueRole.PARTICIPANT);
        userLeagueRoleRepository.save(participantRole);

        league.setNumberOfPlayers(league.getNumberOfPlayers() + 1);
        League updatedLeague = leagueRepository.save(league);

        return mapToLeagueResponseDto(updatedLeague);
    }

    // Método para obtener una liga por su ID
    @Transactional
    public LeagueResponseDto getLeagueById(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        return mapToLeagueResponseDto(league);
    }

    /**
     * Implementación del método para actualizar una liga.
     * @param id El ID de la liga a actualizar.
     * @param leagueCreateDto DTO con los datos actualizados.
     * @return DTO de la liga actualizada.
     */
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

    /**
     * Implementación del método para eliminar una liga.
     * @param id El ID de la liga a eliminar.
     */
    @Transactional
    public void deleteLeague(Long id) {
        if (!leagueRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada");
        }
        leagueRepository.deleteById(id);
    }

    // Método para verificar si un usuario es administrador de una liga
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