package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.config.SecurityConfiguration;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PasswordUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.service.UserService;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private final long MOCK_USER_ID = 1L;
    private final long ANOTHER_USER_ID = 2L;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        @Primary
        public CustomUserDetailsService customUserDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean
        @Primary
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        @Primary
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = TestUserSecurityContextFactory.class)
    public @interface WithTestUser {
        long id();

        String username() default "testuser";
    }

    static class TestUserSecurityContextFactory implements WithSecurityContextFactory<WithTestUser> {
        @Override
        public SecurityContext createSecurityContext(WithTestUser annotation) {
            CustomUserDetails userDetails = new CustomUserDetails(annotation.id(), annotation.username(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            return context;
        }
    }

    @Test
    @WithAnonymousUser
    void endpoints_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/" + MOCK_USER_ID)).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/users/" + MOCK_USER_ID).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_whenUpdatingAnotherUser_shouldReturnForbidden() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("hacker");
        updateDto.setEmail("hacker@example.com");

        mockMvc.perform(put("/api/users/" + ANOTHER_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_whenUpdatingAnotherUser_shouldReturnForbidden() throws Exception {
        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPassword123");
        passwordDto.setNewPassword("newPassword123");

        mockMvc.perform(put("/api/users/" + ANOTHER_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_whenDeletingAnotherUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/" + ANOTHER_USER_ID).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserById_whenUserExists_shouldReturnOkAndUserData() throws Exception {
        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername("testuser");

        when(userService.getUserById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/" + MOCK_USER_ID)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is((int) MOCK_USER_ID))).andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(MOCK_USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/" + MOCK_USER_ID)).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_whenRequestIsValid_shouldReturnOk() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newuser");
        updateDto.setEmail("new@example.com");

        User updatedUser = new User();
        updatedUser.setId(MOCK_USER_ID);
        updatedUser.setUsername("newuser");

        when(userService.updateUser(eq(MOCK_USER_ID), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.username", is("newuser")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_whenServiceThrowsNotFound_shouldReturnNotFound() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newuser");
        updateDto.setEmail("new@example.com");

        when(userService.updateUser(eq(MOCK_USER_ID), any(UserUpdateDto.class))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_withInvalidDto_shouldReturnBadRequest() throws Exception {
        UserUpdateDto invalidDto = new UserUpdateDto();
        invalidDto.setUsername("");
        invalidDto.setEmail("not-an-email");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_whenRequestIsValid_shouldReturnOk() throws Exception {
        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPassword123");
        passwordDto.setNewPassword("newPassword123");

        when(userService.updatePassword(eq(MOCK_USER_ID), any(PasswordUpdateDto.class))).thenReturn(new User());

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_whenServiceThrowsBadRequest_shouldReturnBadRequest() throws Exception {
        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("wrongOld");
        passwordDto.setNewPassword("newPassword123");

        when(userService.updatePassword(eq(MOCK_USER_ID), any(PasswordUpdateDto.class))).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña antigua es incorrecta"));

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_withInvalidDto_shouldReturnBadRequest() throws Exception {
        PasswordUpdateDto invalidDto = new PasswordUpdateDto();
        invalidDto.setOldPassword("oldpass");
        invalidDto.setNewPassword("short");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidDto))).andExpect(status().isBadRequest());
    }


    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_whenSuccessful_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(MOCK_USER_ID);

        mockMvc.perform(delete("/api/users/" + MOCK_USER_ID).with(csrf())).andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(MOCK_USER_ID);
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(userService).deleteUser(MOCK_USER_ID);

        mockMvc.perform(delete("/api/users/" + MOCK_USER_ID).with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getCurrentUser_whenAuthenticated_shouldReturnUserData() throws Exception {
        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername("testuser");
        testUser.setProfileImageUrl("https://example.com/profile.png");

        when(userService.getUserById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) MOCK_USER_ID)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.profileImageUrl", is("https://example.com/profile.png")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getCurrentUser_whenUserNotFoundInService_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(MOCK_USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }
}