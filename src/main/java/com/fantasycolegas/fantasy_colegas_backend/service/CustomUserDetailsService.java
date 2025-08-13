package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Implementación de {@link UserDetailsService} para la carga de usuarios.
 * <p>
 * Este servicio es utilizado por Spring Security para buscar y cargar los detalles
 * de un usuario a partir de su nombre de usuario o correo electrónico.
 * </p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Carga un usuario por su nombre de usuario o dirección de correo electrónico.
     * <p>
     * Este método es crucial para el proceso de autenticación de Spring Security.
     * Busca un usuario en la base de datos y, si lo encuentra, crea un objeto
     * {@link CustomUserDetails} para la sesión.
     * </p>
     *
     * @param usernameOrEmail El nombre de usuario o correo electrónico del usuario.
     * @return Una instancia de {@link UserDetails} con los datos del usuario.
     * @throws UsernameNotFoundException Si no se encuentra un usuario con el nombre
     *                                   o correo electrónico proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        return CustomUserDetails.build(user);
    }
}