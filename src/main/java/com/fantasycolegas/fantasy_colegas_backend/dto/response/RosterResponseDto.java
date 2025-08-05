package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import com.fantasycolegas.fantasy_colegas_backend.model.PlayerTeamRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RosterResponseDto {
    private Long playerId;
    private String playerName;
    private PlayerTeamRole role;
    private String playerImage;
}