package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la actualización de los puntos totales de un jugador.
 * <p>
 * Este objeto se utiliza para transferir la nueva cantidad de puntos que se
 * asignará a un jugador en el sistema.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsUpdateDto {

    private int totalPoints;
}