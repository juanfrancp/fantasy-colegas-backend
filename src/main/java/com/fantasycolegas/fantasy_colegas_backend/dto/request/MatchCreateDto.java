package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de creación de un nuevo partido.
 * <p>
 * Contiene los datos necesarios para programar un partido en una liga específica,
 * como el ID de la liga, la fecha y una descripción opcional.
 * </p>
 */
@Data
public class MatchCreateDto {

    @NotNull(message = "El ID de la liga es obligatorio.")
    private Long leagueId;

    @NotNull(message = "La fecha del partido es obligatoria.")
    private LocalDate matchDate;

    private String description;
}