package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class JoinLeagueDto {

    @NotBlank(message = "El código de invitación no puede estar vacío")
    private String joinCode;

    // Getters y Setters
    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
}