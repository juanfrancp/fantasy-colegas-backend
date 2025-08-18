package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private final Long MOCK_USER_ID = 1L;
    private final Long NON_EXISTENT_USER_ID = 99L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(MOCK_USER_ID);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedOldPassword");
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUserById(MOCK_USER_ID);
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnEmptyOptional() {
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());
        Optional<User> foundUser = userService.getUserById(NON_EXISTENT_USER_ID);
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void updateUser_whenDataIsValid_shouldUpdateAndReturnUser() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newusername");
        updateDto.setEmail("new@example.com");

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newusername")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(MOCK_USER_ID, updateDto);

        assertEquals("newusername", updatedUser.getUsername());
        assertEquals("new@example.com", updatedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowNotFoundException() {
        UserUpdateDto updateDto = new UserUpdateDto();
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(NON_EXISTENT_USER_ID, updateDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateUser_whenEmailAlreadyExists_shouldThrowBadRequestException() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("existing@example.com");
        User anotherUser = new User();
        anotherUser.setId(2L);

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(anotherUser));

        var exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(MOCK_USER_ID, updateDto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El email ya está en uso"));
    }

    @Test
    void updateUser_whenUsernameAlreadyExists_shouldThrowBadRequestException() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("existinguser");
        User anotherUser = new User();
        anotherUser.setId(2L);

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(anotherUser));

        var exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(MOCK_USER_ID, updateDto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("El nombre de usuario ya está en uso"));
    }

    @Test
    void updateUser_withNoChanges_shouldNotChangeData() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername(user.getUsername());
        updateDto.setEmail(user.getEmail());

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(MOCK_USER_ID, updateDto);

        assertEquals(user.getUsername(), updatedUser.getUsername());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }


    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        when(userRepository.existsById(MOCK_USER_ID)).thenReturn(true);
        doNothing().when(userRepository).deleteById(MOCK_USER_ID);

        assertDoesNotThrow(() -> userService.deleteUser(MOCK_USER_ID));

        verify(userRepository, times(1)).deleteById(MOCK_USER_ID);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowNotFoundException() {
        when(userRepository.existsById(NON_EXISTENT_USER_ID)).thenReturn(false);

        var exception = assertThrows(ResponseStatusException.class, () -> userService.deleteUser(NON_EXISTENT_USER_ID));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }
}