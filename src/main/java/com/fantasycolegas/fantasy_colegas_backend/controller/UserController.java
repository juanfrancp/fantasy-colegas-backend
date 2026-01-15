package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PasswordUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserRoleUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserUpdateResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.FileStorageService;
import com.fantasycolegas.fantasy_colegas_backend.service.UserService;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateDto passwordUpdateDto) {
        User updatedUser = userService.updatePassword(id, passwordUpdateDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return userService.getUserById(currentUser.getId())
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getProfileImageUrl(), user.getAppRole()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserUpdateResponseDto> updateCurrentUserProfile(@RequestBody @Valid UserUpdateDto userUpdateDto, Authentication authentication) {
        String currentUsername = authentication.getName();
        UserUpdateResponseDto response = userService.updateUserAndGenerateNewToken(currentUsername, userUpdateDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> uploadProfileImage(@RequestParam("image") MultipartFile file, Authentication authentication) {
        String username = authentication.getName();

        UserResponseDto responseDto = userService.updateUserProfileImage(username, file);

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUserRole(@PathVariable Long id, @RequestBody UserRoleUpdateDto dto) {
        User updatedUser = userService.changeUserRole(id, dto.getNewRole());
        UserResponseDto responseDto = new UserResponseDto(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getProfileImageUrl(),
                updatedUser.getAppRole()
        );

        return ResponseEntity.ok(responseDto);
    }
}