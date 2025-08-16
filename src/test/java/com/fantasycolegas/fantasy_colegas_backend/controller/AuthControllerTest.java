package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.LoginDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RegisterDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.AuthResponse;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.service.AuthService;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // <- Importación añadida
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // <- Importación añadida
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean
        public AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean
        public CustomUserDetailsService userDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }
    }

    @Test
    void registerUser_WhenRegistrationIsSuccessful_ShouldReturnOkStatus() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        when(authService.registerUser(any(RegisterDto.class))).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void registerUser_WhenRegistrationFails_ShouldReturnBadRequestStatus() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("existinguser");
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password123");

        when(authService.registerUser(any(RegisterDto.class))).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username or email already in use"));
    }

    @Test
    void createAuthenticationToken_WhenLoginIsSuccessful_ShouldReturnOkStatusAndJwt() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password123");
        AuthResponse authResponse = new AuthResponse("mocked_jwt_token");
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mocked_jwt_token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"jwt\":\"mocked_jwt_token\"}"));
    }

    @Test
    void createAuthenticationToken_WhenLoginFails_ShouldReturnBadRequestStatus() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("wronguser");
        loginDto.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect username or password")); // <- Excepción corregida

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect username or password"));
    }

    @Test
    void registerUser_WhenEmailIsInvalid_ShouldReturnBadRequestStatus() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("invalidemailuser");
        registerDto.setEmail("invalid-email");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_WhenUsernameIsBlank_ShouldReturnBadRequestStatus() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("");
        registerDto.setEmail("user@example.com");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAuthenticationToken_WhenAccountIsDisabled_ShouldReturnBadRequestStatus() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("disableduser");
        loginDto.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.DisabledException("User is disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect username or password"));
    }

    @Test
    void registerUser_WhenServiceThrowsUnexpectedException_ShouldReturnServerError() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("erroruser");
        registerDto.setEmail("error@example.com");
        registerDto.setPassword("password123");

        when(authService.registerUser(any(RegisterDto.class))).thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}