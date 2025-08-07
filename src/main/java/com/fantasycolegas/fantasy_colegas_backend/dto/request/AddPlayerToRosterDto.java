package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import lombok.Data;

@Data
public class AddPlayerToRosterDto {
    private Long playerId;
    private PlayerTeamRole position;
}