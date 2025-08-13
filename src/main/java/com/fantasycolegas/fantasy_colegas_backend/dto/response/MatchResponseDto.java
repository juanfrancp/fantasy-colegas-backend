package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate matchDate;
    private Long leagueId;
    private String leagueName;
}