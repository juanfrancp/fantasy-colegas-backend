package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;

@Data
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}