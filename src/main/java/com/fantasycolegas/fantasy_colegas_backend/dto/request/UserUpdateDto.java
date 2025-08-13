package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de actualización de un usuario.
 * <p>
 * Este objeto se utiliza para transferir los datos del usuario que pueden ser
 * actualizados, como el nombre de usuario, correo electrónico y contraseña.
 * </p>
 */
@Data
public class UserUpdateDto {
    private String username;
    private String email;
    private String password;
}