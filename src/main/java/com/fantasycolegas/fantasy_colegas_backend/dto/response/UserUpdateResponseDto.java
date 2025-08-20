package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateResponseDto {
    private UserResponseDto user;
    private String newJwt;
}
