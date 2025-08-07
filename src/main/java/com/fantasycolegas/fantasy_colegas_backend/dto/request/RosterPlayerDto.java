package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RosterPlayerDto {

    @NotNull(message = "El ID del jugador no puede ser nulo.")
    private Long playerId;

    @NotNull(message = "El rol del jugador no puede ser nulo.")
    private PlayerTeamRole role;
}