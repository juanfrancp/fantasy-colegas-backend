package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlayerCreateDto {

    @NotBlank(message = "El nombre del jugador no puede estar vacío")
    private String name;

    private String image;
}