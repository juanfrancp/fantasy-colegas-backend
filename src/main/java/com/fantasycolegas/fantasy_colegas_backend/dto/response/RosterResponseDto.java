package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de los datos completos de un equipo (roster).
 * <p>
 * Este objeto se utiliza para transferir una representación completa del equipo
 * de un usuario, incluyendo los datos del usuario, la liga y la lista de jugadores
 * que componen el equipo.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RosterResponseDto {
    private Long userId;
    private String username;
    private Long leagueId;
    private String leagueName;
    private List<RosterPlayerResponseDto> players;
}