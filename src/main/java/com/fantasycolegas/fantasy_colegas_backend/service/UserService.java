package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PasswordUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserUpdateResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.AppRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailsService userDetailsService, JwtUtil jwtUtil, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.fileStorageService = fileStorageService;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 1. Validar el email
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(userUpdateDto.getEmail()).ifPresent(u -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está en uso");
            });
            user.setEmail(userUpdateDto.getEmail());
        }

        // 2. Validar el username
        if (userUpdateDto.getUsername() != null && !userUpdateDto.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(userUpdateDto.getUsername()).ifPresent(u -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario ya está en uso");
            });
            user.setUsername(userUpdateDto.getUsername());
        }

        if (userUpdateDto.getProfileImageUrl() != null && !userUpdateDto.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(userUpdateDto.getProfileImageUrl());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(Long id, PasswordUpdateDto passwordUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 1. Verifica si la contraseña antigua es incorrecta.
        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña antigua es incorrecta");
        }

        // 2. Verifica si la contraseña nueva es la misma que la antigua
        if (passwordEncoder.matches(passwordUpdateDto.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña no puede ser la misma que la antigua.");
        }

        String newEncodedPassword = passwordEncoder.encode(passwordUpdateDto.getNewPassword());
        user.setPassword(newEncodedPassword);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    public User updateUserByUsername(String username, UserUpdateDto userUpdateDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (userUpdateDto.getUsername() != null && !userUpdateDto.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userUpdateDto.getUsername()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario ya está en uso");
            }
            user.setUsername(userUpdateDto.getUsername());
        }

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userUpdateDto.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está en uso");
            }
            user.setEmail(userUpdateDto.getEmail());
        }

        return userRepository.save(user);
    }

    public UserUpdateResponseDto updateUserAndGenerateNewToken(String currentUsername, UserUpdateDto userUpdateDto) {
        User updatedUser = this.updateUserByUsername(currentUsername, userUpdateDto);

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(updatedUser.getId());
        userDto.setUsername(updatedUser.getUsername());
        userDto.setEmail(updatedUser.getEmail());
        userDto.setProfileImageUrl(updatedUser.getProfileImageUrl());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(updatedUser.getUsername());
        String newJwt = jwtUtil.generateToken(userDetails);

        return new UserUpdateResponseDto(userDto, newJwt);
    }

    public UserResponseDto updateUserProfileImage(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String fileName = fileStorageService.storeFile(file, "profile-pics");

        user.setProfileImageUrl(fileName);
        User updatedUser = userRepository.save(user);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(updatedUser.getId());
        responseDto.setUsername(updatedUser.getUsername());
        responseDto.setEmail(updatedUser.getEmail());
        responseDto.setProfileImageUrl(updatedUser.getProfileImageUrl());

        return responseDto;
    }

    public User updateProfileImage(String username, String imageUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.setProfileImageUrl(imageUrl);

        return userRepository.save(user);
    }

    public User changeUserRole(Long userId, AppRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        user.setAppRole(newRole);
        return userRepository.save(user);
    }
}