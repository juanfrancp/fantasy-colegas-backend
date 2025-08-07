package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class MatchCreateDto {

    @NotNull(message = "El ID de la liga es obligatorio.")
    private Long leagueId;

    @NotNull(message = "La fecha del partido es obligatoria.")
    private LocalDate matchDate;

    private String description;
}