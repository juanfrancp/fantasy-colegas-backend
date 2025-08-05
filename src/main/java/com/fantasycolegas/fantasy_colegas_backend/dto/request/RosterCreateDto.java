package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RosterCreateDto {

    @NotNull(message = "La lista de jugadores no puede ser nula.")
    @Valid
    private List<RosterPlayerDto> players;
}