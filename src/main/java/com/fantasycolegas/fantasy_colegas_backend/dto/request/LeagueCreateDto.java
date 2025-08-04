package com.fantasycolegas.fantasy_colegas_backend.dto.request;

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
}