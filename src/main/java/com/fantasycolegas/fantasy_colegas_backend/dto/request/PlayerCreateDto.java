package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de creación de un nuevo jugador.
 * <p>
 * Contiene los datos necesarios para crear un jugador en el sistema,
 * como su nombre e imagen.
 * </p>
 */
@Data
public class PlayerCreateDto {

    @NotBlank(message = "El nombre del jugador no puede estar vacío")
    private String name;

    private String image;
}