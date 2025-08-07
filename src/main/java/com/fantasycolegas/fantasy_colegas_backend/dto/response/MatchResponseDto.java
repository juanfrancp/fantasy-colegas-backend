package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate matchDate;
    private Long leagueId; // Referencia simple a la liga
    private String leagueName;
}