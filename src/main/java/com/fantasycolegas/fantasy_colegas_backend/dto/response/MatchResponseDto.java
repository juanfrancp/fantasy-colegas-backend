package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.MatchStatus; // Asegúrate de importar el Enum
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MatchResponseDto {
    private Long id;
    private Long leagueId; // <--- FALTABA ESTE
    private LocalDateTime matchDate;
    private MatchStatus status; // <--- FALTABA ESTE
    private Integer homeScore;
    private Integer awayScore;
    private MatchTeamResponseDto homeTeam;
    private MatchTeamResponseDto awayTeam;
}