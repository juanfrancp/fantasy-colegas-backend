package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de unirse a una liga.
 * <p>
 * Contiene el código de invitación necesario para que un usuario pueda unirse a
 * una liga privada.
 * </p>
 */
@Data
public class JoinLeagueDto {

    @NotBlank(message = "El código de invitación no puede estar vacío")
    private String joinCode;
}