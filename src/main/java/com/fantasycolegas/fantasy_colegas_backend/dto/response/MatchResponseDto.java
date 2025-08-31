package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponseDto {
    private Long id;
    private MatchTeamResponseDto homeTeam;
    private MatchTeamResponseDto awayTeam;
    private Integer homeScore;
    private Integer awayScore;
    private LocalDateTime matchDate;
}