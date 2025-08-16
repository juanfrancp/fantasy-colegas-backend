package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.config.SecurityConfiguration;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PasswordUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.UserUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(UserController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final long MOCK_USER_ID = 1L;
    private final String MOCK_USERNAME = "testuser";

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public UserRepository userRepository() {
            return mock(UserRepository.class);
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
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = TestUserSecurityContextFactory.class)
    public @interface WithTestUser {
        long id();

        String username() default "testuser";

        String password() default "password";

        String[] roles() default {"USER"};
    }

    static class TestUserSecurityContextFactory implements WithSecurityContextFactory<WithTestUser> {
        @Override
        public SecurityContext createSecurityContext(WithTestUser annotation) {
            long userId = annotation.id();
            String username = annotation.username();
            Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            CustomUserDetails userDetails = new CustomUserDetails(userId, username, annotation.password(), authorities);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, annotation.password(), authorities);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            return context;
        }
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserById_shouldReturnUser_whenIdIsValid() throws Exception {
        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/" + MOCK_USER_ID)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is((int) MOCK_USER_ID))).andExpect(jsonPath("$.username", is(MOCK_USERNAME)));
    }


    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnUpdatedUser_whenUserUpdatesSelf() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newusername");
        updateDto.setEmail("newemail@example.com");

        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail("test@example.com");

        User updatedUser = new User();
        updatedUser.setId(MOCK_USER_ID);
        updatedUser.setUsername("newusername");
        updatedUser.setEmail("newemail@example.com");

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.username", is("newusername"))).andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnUpdatedUser_whenOldPasswordIsCorrect() throws Exception {
        User mockUser = new User();
        mockUser.setId(MOCK_USER_ID);
        mockUser.setPassword(passwordEncoder.encode("oldPassword"));

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPassword");
        passwordDto.setNewPassword("newPassword");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isOk()).andExpect(jsonPath("$.username").value(mockUser.getUsername()));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_shouldReturnNoContent_whenUserDeletesSelf() throws Exception {
        when(userRepository.existsById(MOCK_USER_ID)).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        mockMvc.perform(delete("/api/users/" + MOCK_USER_ID).with(csrf())).andExpect(status().isNoContent());

        verify(userRepository, times(1)).deleteById(MOCK_USER_ID);
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnForbidden_whenUserIdDoesNotExist() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("nonexistentuser");

        mockMvc.perform(put("/api/users/9999").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnForbidden_whenUserIdDoesNotExist() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPassword");
        passwordDto.setNewPassword("newPassword");

        mockMvc.perform(put("/api/users/9999/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_shouldReturnForbidden_whenUserIdDoesNotExist() throws Exception {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/users/9999").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnForbidden_whenUpdatingAnotherUser() throws Exception {
        long anotherUserId = 2L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("hacker");

        mockMvc.perform(put("/api/users/" + anotherUserId).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnForbidden_whenUpdatingAnotherUser() throws Exception {
        long anotherUserId = 2L;
        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPassword");
        passwordDto.setNewPassword("newPassword");

        mockMvc.perform(put("/api/users/" + anotherUserId + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_shouldReturnForbidden_whenDeletingAnotherUser() throws Exception {
        long anotherUserId = 2L;

        mockMvc.perform(delete("/api/users/" + anotherUserId).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void getUserById_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/" + MOCK_USER_ID)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnBadRequest_whenOldPasswordIsIncorrect() throws Exception {
        User mockUser = new User();
        mockUser.setId(MOCK_USER_ID);
        mockUser.setPassword(passwordEncoder.encode("oldPasswordCorrecta"));

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(mockUser));

        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPasswordIncorrecta");
        passwordDto.setNewPassword("newPassword");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("existingemail@example.com");

        when(userRepository.findByEmail("existingemail@example.com")).thenReturn(Optional.of(new User()));
        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(new User()));

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserById_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        when(userRepository.findById(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/9999")).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("invalid-email");
        updateDto.setUsername("validusername");

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(new User()));

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnBadRequest_whenNewPasswordIsInvalid() throws Exception {
        User mockUser = new User();
        mockUser.setId(MOCK_USER_ID);
        mockUser.setPassword(passwordEncoder.encode("oldPassword"));

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(mockUser));

        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("oldPassword");
        passwordDto.setNewPassword("123");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnBadRequest_whenNewPasswordIsSameAsOld() throws Exception {
        User mockUser = new User();
        mockUser.setId(MOCK_USER_ID);
        String encodedPassword = passwordEncoder.encode("samePassword");
        mockUser.setPassword(encodedPassword);

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(mockUser));

        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("samePassword");
        passwordDto.setNewPassword("samePassword");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnBadRequest_whenUsernameIsEmpty() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("");
        updateDto.setEmail("test@example.com");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnBadRequest_whenUsernameAlreadyExists() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("existingUser");
        updateDto.setEmail("newemail@example.com");

        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser));
        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(new User()));

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldUpdateUsernameOnly_whenEmailIsNull() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newusername");
        updateDto.setEmail(null);

        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail("test@example.com");

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.username", is("newusername"))).andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePassword_shouldReturnBadRequest_whenNewPasswordIsNullOrEmpty() throws Exception {
        User mockUser = new User();
        mockUser.setId(MOCK_USER_ID);
        mockUser.setPassword(passwordEncoder.encode("oldPassword"));

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(mockUser));

        PasswordUpdateDto passwordDtoNull = new PasswordUpdateDto();
        passwordDtoNull.setOldPassword("oldPassword");
        passwordDtoNull.setNewPassword(null);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDtoNull))).andExpect(status().isBadRequest());

        PasswordUpdateDto passwordDtoEmpty = new PasswordUpdateDto();
        passwordDtoEmpty.setOldPassword("oldPassword");
        passwordDtoEmpty.setNewPassword("");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDtoEmpty))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnBadRequest_whenUsernameIsNullOrEmpty() throws Exception {
        UserUpdateDto updateDtoEmpty = new UserUpdateDto();
        updateDtoEmpty.setUsername("");
        updateDtoEmpty.setEmail("validemail@example.com");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDtoEmpty))).andExpect(status().isBadRequest());

        UserUpdateDto updateDtoNull = new UserUpdateDto();
        updateDtoNull.setUsername(null);
        updateDtoNull.setEmail("validemail@example.com");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDtoNull))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnOk_whenNoChangesAreMade() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername(MOCK_USERNAME);
        updateDto.setEmail("test@example.com");

        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail("test@example.com");

        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.username", is(MOCK_USERNAME))).andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void updateUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("unauthuser");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isUnauthorized());
    }

    @Test
    void updatePassword_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        PasswordUpdateDto passwordDto = new PasswordUpdateDto();
        passwordDto.setOldPassword("old");
        passwordDto.setNewPassword("new");

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID + "/password").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(passwordDto))).andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUser_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/users/" + MOCK_USER_ID).with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldUpdateEmailOnly_whenUsernameIsTheSame() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername(MOCK_USERNAME);
        updateDto.setEmail("newemail@example.com");

        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail("test@example.com");

        User updatedUser = new User();
        updatedUser.setId(MOCK_USER_ID);
        updatedUser.setUsername(MOCK_USERNAME);
        updatedUser.setEmail("newemail@example.com");

        when(userRepository.findByUsername(MOCK_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.username", is(MOCK_USERNAME))).andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteUser_shouldReturnNotFound_whenUserExistsButDoesNotExistInDatabase() throws Exception {
        when(userRepository.existsById(MOCK_USER_ID)).thenReturn(false);

        mockMvc.perform(delete("/api/users/" + MOCK_USER_ID).with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnOk_whenUpdatingUsernameToSameValue() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername(MOCK_USERNAME);
        updateDto.setEmail("test@example.com");

        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail("test@example.com");

        when(userRepository.findByUsername(MOCK_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateUser_shouldReturnOk_whenUpdatingEmailToSameValue() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername(MOCK_USERNAME);
        updateDto.setEmail("test@example.com");

        User testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(MOCK_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/" + MOCK_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk());
    }
}