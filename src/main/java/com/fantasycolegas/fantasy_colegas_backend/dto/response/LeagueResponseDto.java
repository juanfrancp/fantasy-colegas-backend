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
 * DTO (Data Transfer Object) para la respuesta de los datos de una liga.
 * <p>
 * Este objeto se utiliza para transferir una representación completa de una liga,
 * incluyendo sus detalles básicos, sus administradores, participantes, jugadores
 * y otras configuraciones.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResponseDto {
    private Long id;
    private String name;
    private String description;
    private String image;
    private boolean isPrivate;
    private String joinCode;
    private int numberOfPlayers;
    private List<UserResponseDto> admins;
    private List<UserResponseDto> participants;
    private int teamSize;
    private List<PlayerResponseDto> players;
}