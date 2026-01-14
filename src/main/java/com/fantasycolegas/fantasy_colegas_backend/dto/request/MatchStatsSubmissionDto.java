package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class MatchStatsSubmissionDto {
    private int homeScore;
    private int awayScore;
    private List<PlayerMatchStatsUpdateDto> playerStats;
}