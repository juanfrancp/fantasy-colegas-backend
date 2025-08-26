package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStandingsDto {
    private Long userId;
    private String username;
    private String profileImageUrl;
    private int totalPoints;
}