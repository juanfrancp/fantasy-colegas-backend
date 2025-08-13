package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.RegisterDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Servicio para la gestión de la autenticación de usuarios.
 * <p>
 * Proporciona funcionalidades como el registro de nuevos usuarios.
 * </p>
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Verifica si el nombre de usuario o el correo electrónico ya existen.
     * Si no, crea un nuevo usuario con la contraseña codificada y lo guarda en la base de datos.
     * </p>
     *
     * @param registerDto DTO que contiene los datos de registro (nombre de usuario, email, contraseña).
     * @return {@code true} si el registro fue exitoso, {@code false} si el usuario ya existía.
     */
    public boolean registerUser(RegisterDto registerDto) {
        if (userRepository.findByUsernameOrEmail(registerDto.getUsername(), registerDto.getEmail()).isPresent()) {
            return false;
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        userRepository.save(user);
        return true;
    }
}