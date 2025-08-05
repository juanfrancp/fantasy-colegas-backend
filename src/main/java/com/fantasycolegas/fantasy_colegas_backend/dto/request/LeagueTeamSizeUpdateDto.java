package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LeagueTeamSizeUpdateDto {

    @Min(value = 3, message = "El tamaño mínimo del equipo es 3")
    @Max(value = 11, message = "El tamaño máximo del equipo es 11")
    private int teamSize;
}