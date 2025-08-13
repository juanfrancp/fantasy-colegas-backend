package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 *
 * DTO (Data Transfer Object) para representar un jugador dentro de un equipo (roster).
 * <p>
 * Este objeto se utiliza para especificar el jugador y su rol en un equipo de fantasía,
 * típicamente dentro de un {@link RosterCreateDto}.
 * </p>
 */
@Data
public class RosterPlayerDto {

    @NotNull(message = "El ID del jugador no puede ser nulo.")
    private Long playerId;

    @NotNull(message = "El rol del jugador no puede ser nulo.")
    private PlayerTeamRole role;
}