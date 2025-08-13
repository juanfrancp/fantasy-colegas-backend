package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la creación de un equipo de fantasía (roster).
 * <p>
 * Este objeto se utiliza para transferir la lista completa de jugadores que
 * conformarán el equipo de un usuario en una liga.
 * </p>
 */
@Data
public class RosterCreateDto {

    @NotNull(message = "La lista de jugadores no puede ser nula.")
    @Valid
    private List<RosterPlayerDto> players;
}