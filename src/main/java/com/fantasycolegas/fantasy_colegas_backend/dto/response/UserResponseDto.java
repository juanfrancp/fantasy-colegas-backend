package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de datos de un usuario.
 * <p>
 * Este objeto se utiliza para transferir una representación de un usuario,
 * normalmente en un contexto en el que se necesita información básica,
 * como el ID y el nombre de usuario, sin exponer datos sensibles.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
}