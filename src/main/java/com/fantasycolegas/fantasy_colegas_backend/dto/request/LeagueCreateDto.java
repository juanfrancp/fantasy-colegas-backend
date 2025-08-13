package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la creación de una nueva liga.
 * <p>
 * Contiene los datos necesarios para crear una liga, como el nombre, descripción,
 * tamaño del equipo, etc. Incluye validaciones para asegurar que los datos
 * proporcionados sean correctos.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueCreateDto {

    @NotBlank(message = "El nombre de la liga es obligatorio")
    private String name;

    private String description;

    private String image;

    private boolean isPrivate;

    private int numberOfPlayers;

    @Min(value = 3, message = "El tamaño mínimo del equipo es 3")
    @Max(value = 11, message = "El tamaño máximo del equipo es 11")
    private int teamSize;
}