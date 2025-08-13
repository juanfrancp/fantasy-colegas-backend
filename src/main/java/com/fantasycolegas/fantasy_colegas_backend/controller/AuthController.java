package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LoginDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RegisterDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.AuthResponse;
import com.fantasycolegas.fantasy_colegas_backend.service.AuthService;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 *
 * Controlador REST para la autenticación de usuarios.
 * <p>
 * Proporciona endpoints para el inicio de sesión y el registro de nuevos usuarios en la aplicación.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Gestor de autenticación de Spring Security.
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Servicio personalizado para cargar los detalles del usuario.
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Utilidad para la generación y gestión de tokens JWT.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Servicio de autenticación para la lógica de negocio de registro.
     */
    @Autowired
    private AuthService authService;

    /**
     * Endpoint para la autenticación y generación de un token JWT.
     * <p>
     * Autentica al usuario con las credenciales proporcionadas. Si son válidas,
     * genera un token JWT y lo devuelve en la respuesta.
     * </p>
     *
     * @param loginDto DTO con el nombre de usuario/email y la contraseña.
     * @return {@link ResponseEntity} que contiene un objeto {@link AuthResponse} con el token JWT.
     * @throws Exception Si las credenciales son incorrectas.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDto loginDto) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()));
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUsernameOrEmail());

        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    /**
     * Endpoint para registrar un nuevo usuario en la aplicación.
     * <p>
     * Registra un nuevo usuario utilizando la información proporcionada en el DTO de registro.
     * </p>
     *
     * @param registerDto DTO con la información del nuevo usuario.
     * @return {@link ResponseEntity} con un mensaje de éxito o un error si el registro falla (ej. usuario o email ya existen).
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto registerDto) {
        if (authService.registerUser(registerDto)) {
            return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Username or email already in use", HttpStatus.BAD_REQUEST);
        }
    }
}