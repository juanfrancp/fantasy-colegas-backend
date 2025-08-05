package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import com.fantasycolegas.fantasy_colegas_backend.model.PlayerTeamRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // <-- Constructor de 5 argumentos
@NoArgsConstructor
public class RosterPlayerResponseDto {
    private Long playerId;
    private String playerName;
    private PlayerTeamRole role;
    private String playerImage;
    private Integer totalPoints;
}