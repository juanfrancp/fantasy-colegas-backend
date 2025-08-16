package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
    private String username;

    @Email(message = "El formato del email no es válido.")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;
}