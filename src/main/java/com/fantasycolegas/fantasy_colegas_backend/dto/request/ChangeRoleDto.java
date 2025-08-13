package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de cambio de rol de un usuario en una liga.
 * <p>
 * Contiene los datos necesarios para actualizar el rol de un participante de la liga,
 * como el nuevo rol a asignar.
 * </p>
 */
@Data
public class ChangeRoleDto {

    @NotNull(message = "El nuevo rol no puede ser nulo")
    private LeagueRole newRole;
}