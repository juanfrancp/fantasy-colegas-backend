package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchTeamResponseDto {
    private Long id;
    private String name;
    private List<PlayerResponseDto> players;
}