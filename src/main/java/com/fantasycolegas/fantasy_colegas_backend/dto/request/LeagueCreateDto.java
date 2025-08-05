package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueCreateDto {

    @NotBlank(message = "El nombre de la liga es obligatorio")
    private String name;

    private String description;
    private String image;
    private boolean isPrivate;
    private int numberOfPlayers;

    @Min(value = 3, message = "El tamaño mínimo del equipo es 3")
    @Max(value = 11, message = "El tamaño máximo del equipo es 11")
    private int teamSize;
}