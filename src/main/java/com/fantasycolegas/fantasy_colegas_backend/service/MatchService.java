package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchTeamResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.*;
import com.fantasycolegas.fantasy_colegas_backend.repository.LeagueRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.MatchRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Transactional
    public MatchResponseDto createMatch(MatchCreateDto matchCreateDto) {
        League league = leagueRepository.findById(matchCreateDto.getLeagueId())
                .orElseThrow(() -> new EntityNotFoundException("League not found with id: " + matchCreateDto.getLeagueId()));

        List<Player> homePlayers = playerRepository.findAllById(matchCreateDto.getHomeTeamPlayerIds());
        List<Player> awayPlayers = playerRepository.findAllById(matchCreateDto.getAwayTeamPlayerIds());

        MatchTeam homeTeam = new MatchTeam(matchCreateDto.getHomeTeamName(), homePlayers);
        MatchTeam awayTeam = new MatchTeam(matchCreateDto.getAwayTeamName(), awayPlayers);

        Match match = new Match();
        match.setLeague(league);
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchDate(matchCreateDto.getMatchDate());

        Match savedMatch = matchRepository.save(match);
        return convertToDto(savedMatch);
    }

    public List<MatchResponseDto> getUpcomingMatches() {
        return matchRepository.findByMatchDateAfter(LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MatchResponseDto> getPastMatches() {
        return matchRepository.findByMatchDateBefore(LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MatchResponseDto convertToDto(Match match) {
        return new MatchResponseDto(
                match.getId(),
                convertToTeamDto(match.getHomeTeam()),
                convertToTeamDto(match.getAwayTeam()),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getMatchDate()
        );
    }

    private MatchTeamResponseDto convertToTeamDto(MatchTeam team) {
        if (team == null) return null;

        List<PlayerResponseDto> players = team.getPlayers().stream()
                .map(this::convertPlayerToDto)
                .collect(Collectors.toList());

        return new MatchTeamResponseDto(
                team.getId(),
                team.getName(),
                players
        );
    }

    // --- MÉTODO CORREGIDO ---
    private PlayerResponseDto convertPlayerToDto(Player player) {
        return new PlayerResponseDto(
                player.getId(),
                player.getName(),
                player.getImage(),
                player.getTotalPoints()
        );
    }

    @Transactional
    public MatchResponseDto updateMatch(Long matchId, MatchUpdateDto updateDto) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found with id: " + matchId));

        // Validar que el partido no se haya jugado ya
        if (match.getMatchDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot update a match that has already been played.");
        }

        // Actualizar equipos
        List<Player> homePlayers = playerRepository.findAllById(updateDto.getHomeTeamPlayerIds());
        List<Player> awayPlayers = playerRepository.findAllById(updateDto.getAwayTeamPlayerIds());

        match.getHomeTeam().setName(updateDto.getHomeTeamName());
        match.getHomeTeam().getPlayers().clear();
        match.getHomeTeam().getPlayers().addAll(homePlayers);

        match.getAwayTeam().setName(updateDto.getAwayTeamName());
        match.getAwayTeam().getPlayers().clear();
        match.getAwayTeam().getPlayers().addAll(awayPlayers);

        // Actualizar fecha del partido
        match.setMatchDate(updateDto.getMatchDate());

        Match updatedMatch = matchRepository.save(match);
        return convertToDto(updatedMatch);
    }
}