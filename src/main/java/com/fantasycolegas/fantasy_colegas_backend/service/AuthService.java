package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LoginDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RegisterDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.AuthResponse;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static java.util.regex.Pattern.matches;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Servicio para la gestión de la autenticación de usuarios.
 * <p>
 * Proporciona funcionalidades como el registro de nuevos usuarios y el inicio de sesión.
 * </p>
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Realiza validaciones en el DTO de registro, incluyendo campos nulos/vacíos,
     * longitud de la contraseña, espacios internos y caracteres especiales.
     * Si las validaciones son exitosas, verifica si el nombre de usuario o el correo electrónico
     * ya existen. En caso de que no existan, crea un nuevo usuario con la contraseña codificada
     * y lo guarda en la base de datos.
     * </p>
     *
     * @param registerDto DTO que contiene los datos de registro (nombre de usuario, email, contraseña).
     * @return {@code true} si el registro fue exitoso, {@code false} si el usuario ya existía o los datos no eran válidos.
     */
    public void registerUser(RegisterDto registerDto) {
        // 1. Validación de Contraseña
        if (registerDto.getPassword() == null || registerDto.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres.");
        }

        // 2. Validación de Campos vacíos
        if (registerDto.getUsername() == null || registerDto.getUsername().isBlank() ||
                registerDto.getEmail() == null || registerDto.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los campos son obligatorios.");
        }

        String username = registerDto.getUsername().trim();
        String email = registerDto.getEmail().trim();

        // 3. Validación de Formato (Regex)
        if (username.contains(" ") || !matches("^[a-zA-Z0-9_-]+$", username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario solo puede contener letras, números, guiones y guiones bajos.");
        }

        if (!matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El formato del correo electrónico no es válido.");
        }

        String usernameLowerCase = username.toLowerCase();
        String emailLowerCase = email.toLowerCase();

        // 4. Validación de Duplicados (Solo aquí lanzamos el error de "Ya existe")
        if (userRepository.findByUsernameOrEmail(usernameLowerCase, emailLowerCase).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario o correo electrónico ya están en uso.");
        }

        User user = new User();
        user.setUsername(usernameLowerCase);
        user.setEmail(emailLowerCase);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error de integridad: El usuario ya existe.");
        }
    }

    /**
     * Autentica a un usuario y genera un token JWT.
     * <p>
     * Utiliza el AuthenticationManager para validar las credenciales del usuario.
     * Si la autenticación es exitosa, carga los detalles del usuario y genera un
     * token JWT que devuelve en la respuesta de autenticación.
     * </p>
     *
     * @param loginDto DTO que contiene las credenciales de inicio de sesión (nombre de usuario o email, contraseña).
     * @return Un objeto AuthResponse que contiene el token JWT.
     * @throws Exception si las credenciales son incorrectas o el usuario no existe.
     */
    public AuthResponse login(LoginDto loginDto) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()));
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUsernameOrEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        return new AuthResponse(jwt);
    }
}