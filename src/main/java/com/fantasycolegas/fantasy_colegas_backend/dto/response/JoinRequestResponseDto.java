package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de una solicitud para unirse a una liga.
 * <p>
 * Este objeto se utiliza para transferir los detalles de una solicitud de unión,
 * incluyendo el ID de la solicitud, el ID del usuario, su nombre de usuario
 * y la fecha de la solicitud.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponseDto {

    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime requestDate;

}