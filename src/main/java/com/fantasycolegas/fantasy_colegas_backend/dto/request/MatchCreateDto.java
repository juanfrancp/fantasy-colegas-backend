package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchCreateDto {
    @NotNull
    private Long leagueId;

    @NotEmpty
    private String homeTeamName;

    @NotEmpty
    private String awayTeamName;

    @NotNull
    @Future
    private LocalDateTime matchDate;

    @NotEmpty
    private List<Long> homeTeamPlayerIds;

    @NotEmpty
    private List<Long> awayTeamPlayerIds;
}