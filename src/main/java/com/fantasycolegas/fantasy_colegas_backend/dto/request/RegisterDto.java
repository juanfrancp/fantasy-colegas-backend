package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de registro de un nuevo usuario.
 * <p>
 * Este objeto se utiliza para transferir los datos de registro (nombre de usuario,
 * correo electrónico y contraseña) desde el cliente al servidor.
 * </p>
 */
@Data
public class RegisterDto {
    private String username;
    private String email;
    private String password;
}