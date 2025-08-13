package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RosterResponseDto {
    private Long userId;
    private String username;
    private Long leagueId;
    private String leagueName;
    private List<RosterPlayerResponseDto> players;
}