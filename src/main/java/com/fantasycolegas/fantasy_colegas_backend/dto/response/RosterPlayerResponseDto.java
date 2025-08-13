package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de un jugador en un equipo (roster).
 * <p>
 * Este objeto se utiliza para transferir los detalles de un jugador que forma parte
 * del equipo de un usuario, incluyendo su información básica, el rol asignado en el
 * equipo y sus puntos totales acumulados.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RosterPlayerResponseDto {
    private Long playerId;
    private String playerName;
    private PlayerTeamRole role;
    private String playerImage;
    private Integer totalPoints;
}