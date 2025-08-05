package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponseDto {

    private Long id;
    private String name;
    private String image;
    private int totalPoints;
}