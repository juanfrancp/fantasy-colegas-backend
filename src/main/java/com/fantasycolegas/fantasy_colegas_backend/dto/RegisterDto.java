package com.fantasycolegas.fantasy_colegas_backend.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String username;
    private String email;
    private String password;
}