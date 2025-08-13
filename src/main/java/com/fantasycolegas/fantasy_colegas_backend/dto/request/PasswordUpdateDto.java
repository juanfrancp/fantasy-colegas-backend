package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la solicitud de actualización de la contraseña de un usuario.
 * <p>
 * Contiene la contraseña antigua del usuario y la nueva contraseña, junto con las
 * validaciones necesarias para asegurar que la nueva contraseña cumpla con los
 * requisitos de seguridad.
 * </p>
 */
@Data
public class PasswordUpdateDto {


    @NotBlank(message = "La contraseña antigua no puede estar vacía")
    private String oldPassword;


    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String newPassword;

}