package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de los datos de un partido.
 * <p>
 * Este objeto se utiliza para transferir una representación de un partido,
 * incluyendo su información básica y los datos de la liga a la que pertenece.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate matchDate;
    private Long leagueId;
    private String leagueName;
}