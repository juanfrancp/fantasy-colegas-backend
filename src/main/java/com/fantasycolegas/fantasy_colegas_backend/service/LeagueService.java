package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;

    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LeagueResponseDto joinLeague(String joinCode, Long userId) {
        Optional<League> optionalLeague = leagueRepository.findByJoinCode(joinCode);
        if (optionalLeague.isEmpty()) {
            throw new RuntimeException("Código de invitación inválido o la liga no existe.");
        }

        League league = optionalLeague.get();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado.");
        }
        User user = optionalUser.get();

        Set<User> participants = league.getParticipants();
        if (participants.contains(user)) {
            throw new RuntimeException("El usuario ya es un participante de esta liga.");
        }

        participants.add(user);
        league.setNumberOfPlayers(participants.size());

        League updatedLeague = leagueRepository.save(league);

        return mapToLeagueResponseDto(updatedLeague);
    }

    @Transactional
    public LeagueResponseDto getLeagueById(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Liga no encontrada."));

        return mapToLeagueResponseDto(league);
    }

    private LeagueResponseDto mapToLeagueResponseDto(League league) {
        // Mapear el administrador
        UserResponseDto adminDto = new UserResponseDto(league.getAdmin().getId(), league.getAdmin().getUsername());

        // Crear una copia defensiva del Set de participantes para evitar el error
        // y luego mapearla a una lista de DTOs.
        List<UserResponseDto> participantsDto = new HashSet<>(league.getParticipants())
                .stream()
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());

        return new LeagueResponseDto(
                league.getId(),
                league.getName(),
                league.getDescription(),
                league.getImage(),
                league.isPrivate(),
                league.getJoinCode(),
                league.getNumberOfPlayers(),
                adminDto,
                participantsDto
        );
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername());
    }
}