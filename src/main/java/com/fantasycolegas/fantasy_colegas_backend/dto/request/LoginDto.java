package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de autenticación de usuario.
 * <p>
 * Este objeto se utiliza para transferir las credenciales de inicio de sesión
 * (nombre de usuario o email y contraseña) desde el cliente al servidor.
 * </p>
 */
@Data
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}