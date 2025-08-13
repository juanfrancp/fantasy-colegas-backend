package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para añadir un jugador a un roster.
 * <p>
 * Esta clase se utiliza para transferir los datos necesarios para agregar
 * un jugador a un equipo dentro de una liga, incluyendo el ID del jugador
 * y la posición que ocupará en el equipo.
 * </p>
 */
@Data
public class AddPlayerToRosterDto {
    private Long playerId;
    private PlayerTeamRole position;
}