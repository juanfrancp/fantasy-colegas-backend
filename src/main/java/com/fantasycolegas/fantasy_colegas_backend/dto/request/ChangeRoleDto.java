package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.LeagueRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleDto {

    @NotNull(message = "El nuevo rol no puede ser nulo")
    private LeagueRole newRole;
}