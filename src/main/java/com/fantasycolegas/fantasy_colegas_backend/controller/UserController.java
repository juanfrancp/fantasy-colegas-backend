package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PasswordUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Controlador REST para la gestión de usuarios.
 * <p>
 * Proporciona endpoints para consultar, actualizar y eliminar usuarios.
 * Los endpoints están protegidos para asegurar que solo el usuario autenticado
 * pueda modificar su propia información.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene los detalles de un usuario por su ID.
     * <p>
     * Requiere que el usuario esté autenticado.
     * </p>
     *
     * @param id El ID del usuario a buscar.
     * @return Una {@link ResponseEntity} con el objeto {@link User} si se encuentra, o un 404 si no.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Actualiza la información de un usuario.
     * <p>
     * Solo permite al usuario autenticado actualizar sus propios datos.
     * </p>
     *
     * @param id            El ID del usuario a actualizar.
     * @param userUpdateDto DTO con los datos del usuario a actualizar.
     * @return Una {@link ResponseEntity} con el objeto {@link User} actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        Optional<User> userOptional = userRepository.findById(id);

        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        if (userUpdateDto.getUsername() != null) {
            user.setUsername(userUpdateDto.getUsername());
        }
        if (userUpdateDto.getEmail() != null) {
            user.setEmail(userUpdateDto.getEmail());
        }
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    /**
     * Actualiza la contraseña de un usuario.
     * <p>
     * Requiere la contraseña antigua para verificar y solo permite al usuario autenticado
     * cambiar su propia contraseña.
     * </p>
     *
     * @param id                El ID del usuario.
     * @param passwordUpdateDto DTO con la contraseña antigua y la nueva.
     * @param currentUser       El usuario autenticado.
     * @return Una {@link ResponseEntity} con el objeto {@link User} actualizado.
     */
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    @PutMapping("/{id}/password")
    public ResponseEntity<User> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateDto passwordUpdateDto, @AuthenticationPrincipal CustomUserDetails currentUser) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña antigua es incorrecta");
        }
        String newEncodedPassword = passwordEncoder.encode(passwordUpdateDto.getNewPassword());
        user.setPassword(newEncodedPassword);
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Elimina un usuario.
     * <p>
     * Solo permite al usuario autenticado eliminar su propia cuenta.
     * </p>
     *
     * @param id El ID del usuario a eliminar.
     * @return Una {@link ResponseEntity} sin contenido si la eliminación es exitosa.
     */
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        userRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}