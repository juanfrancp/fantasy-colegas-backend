package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de actualización del tamaño de equipo de una liga.
 * <p>
 * Contiene el tamaño de equipo a actualizar, con validaciones para asegurar
 * que el valor esté dentro de un rango permitido.
 * </p>
 */
@Data
public class LeagueTeamSizeUpdateDto {

    @Min(value = 3, message = "El tamaño mínimo del equipo es 3")
    @Max(value = 11, message = "El tamaño máximo del equipo es 11")
    private int teamSize;
}