package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinLeagueDto {

    @NotBlank(message = "El código de invitación no puede estar vacío")
    private String joinCode;
}