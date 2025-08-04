package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String username;
    private String email;
    private String password;
}