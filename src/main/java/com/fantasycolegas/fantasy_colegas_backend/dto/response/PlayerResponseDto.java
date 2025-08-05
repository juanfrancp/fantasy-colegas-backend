package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // <-- Constructor de 4 argumentos
@NoArgsConstructor
public class PlayerResponseDto {
    private Long id;
    private String name;
    private String image;
    private Integer totalPoints;
}