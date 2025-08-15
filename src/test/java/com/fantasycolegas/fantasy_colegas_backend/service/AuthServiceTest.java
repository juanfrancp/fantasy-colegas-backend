package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LoginDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RegisterDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.AuthResponse;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para el servicio de autenticación {@link AuthService}.
 * Utiliza Mockito para simular las dependencias y verificar el comportamiento de los métodos de registro y login.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_WhenUserDoesNotExist_ShouldReturnTrueAndSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("testuser", "test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        boolean result = authService.registerUser(registerDto);

        assertTrue(result);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void registerUser_WhenUserAlreadyExists_ShouldReturnFalse() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("existinguser");
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("existinguser", "existing@example.com")).thenReturn(Optional.of(new User()));

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenUsernameAlreadyExists_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("existinguser");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("password123");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsernameOrEmail(eq("existinguser"), eq("newuser@example.com"))).thenReturn(Optional.of(existingUser));

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, times(1)).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenEmailAlreadyExists_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password123");

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByUsernameOrEmail(eq("newuser"), eq("existing@example.com"))).thenReturn(Optional.of(existingUser));

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, times(1)).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenUsernameIsNull_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(null);
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenEmailIsEmpty_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("");
        registerDto.setPassword("password123");

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenSaveThrowsDataIntegrityViolationException_ShouldReturnFalse() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, times(1)).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WhenUsernameExistsWithDifferentCase_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("TestUser");
        registerDto.setEmail("testuser@example.com");
        registerDto.setPassword("password123");

        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("testuser@example.com");
        existingUser.setPassword("encodedPassword");

        when(userRepository.findByUsernameOrEmail("testuser", "testuser@example.com")).thenReturn(Optional.of(existingUser));

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, times(1)).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenPasswordIsTooShort_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("shortpassuser");
        registerDto.setEmail("shortpass@example.com");
        registerDto.setPassword("123");

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenUsernameHasLeadingAndTrailingSpaces_ShouldTrimAndSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("  testuser  ");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("testuser", "test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        boolean result = authService.registerUser(registerDto);

        assertTrue(result);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
    }

    @Test
    void registerUser_WhenUsernameHasInternalSpaces_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("first last");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        boolean result = authService.registerUser(registerDto);

        assertFalse(result);
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenUsernameOrEmailHaveSpecialCharacters_ShouldReturnFalseAndNotSaveUser() {
        RegisterDto registerDto = new RegisterDto();

        registerDto.setUsername("test<script>");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        boolean result = authService.registerUser(registerDto);

        assertFalse(result, "El registro debe fallar por un nombre de usuario con caracteres especiales");
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));

        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test;com");
        registerDto.setPassword("password123");

        result = authService.registerUser(registerDto);

        assertFalse(result, "El registro debe fallar por un email con caracteres especiales");
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WhenCredentialsAreCorrect_ShouldReturnAuthResponse() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password123");

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(loginDto.getUsernameOrEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mocked_jwt_token");

        AuthResponse result = authService.login(loginDto);

        assertNotNull(result);
        assertEquals("mocked_jwt_token", result.getJwt());
        verify(userDetailsService, times(1)).loadUserByUsername(anyString());
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    void login_WhenCredentialsAreIncorrect_ShouldThrowException() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("wronguser");
        loginDto.setPassword("wrongpassword");

        doThrow(new BadCredentialsException("Incorrect")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        Exception exception = assertThrows(Exception.class, () -> authService.login(loginDto));

        assertEquals("Incorrect username or password", exception.getMessage());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void login_WhenUsernameDoesNotExist_ShouldThrowException() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("nonexistentuser");
        loginDto.setPassword("somepassword");

        doThrow(new UsernameNotFoundException("User not found")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        Exception exception = assertThrows(Exception.class, () -> authService.login(loginDto));

        assertEquals("Incorrect username or password", exception.getMessage());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }
}