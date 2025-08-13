package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para representar la puntuación de un usuario.
 * <p>
 * Este objeto se utiliza para transferir el ID de un usuario junto con su
 * puntuación total acumulada en una liga o torneo. Es útil para clasificaciones
 * y tablas de puntos.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScoreDto {

    private Long userId;
    private double totalPoints;
}