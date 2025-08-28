package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplacePlayerDto {
    @NotNull
    private Long playerToRemoveId;

    @NotNull
    private Long playerToAddId;
}