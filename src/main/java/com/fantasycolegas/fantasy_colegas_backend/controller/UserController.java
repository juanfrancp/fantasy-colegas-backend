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

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and #id == principal.id") // Solo el usuario puede actualizar su propio perfil
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

    @PreAuthorize("isAuthenticated() and #id == principal.id")
    @PutMapping("/{id}/password")
    public ResponseEntity<User> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateDto passwordUpdateDto,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {

        // 1. Encontrar el usuario por su ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 2. Verificar que la contraseña antigua coincida
        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña antigua es incorrecta");
        }

        // 3. Codificar y establecer la nueva contraseña
        String newEncodedPassword = passwordEncoder.encode(passwordUpdateDto.getNewPassword());
        user.setPassword(newEncodedPassword);

        // 4. Guardar el usuario actualizado
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("isAuthenticated() and #id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        // 1. Verificar si el usuario existe antes de intentar eliminar
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        // 2. Eliminar el usuario por su ID
        userRepository.deleteById(id);

        // 3. Devolver una respuesta exitosa sin contenido (204 No Content)
        return ResponseEntity.noContent().build();
    }
}