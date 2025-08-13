// Archivo: PlayerUpdateDto.java
package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de actualización de un jugador.
 * <p>
 * Contiene los campos que pueden ser modificados para un jugador existente,
 * como su nombre y la URL de su imagen.
 * </p>
 */
@Data
public class PlayerUpdateDto {

    private String name;
    private String image;
}