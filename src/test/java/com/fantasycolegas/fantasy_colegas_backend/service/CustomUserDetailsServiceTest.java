package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.UserLeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;
    private User testUser;
    private final String MOCK_USERNAME = "testuser";
    private final String MOCK_EMAIL = "test@example.com";
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(MOCK_USERNAME);
        testUser.setEmail(MOCK_EMAIL);
        testUser.setPassword("password");
        UserLeagueRole userLeagueRole = new UserLeagueRole();
        userLeagueRole.setUser(testUser);
        userLeagueRole.setRole(LeagueRole.ADMIN);
        testUser.getLeagueRoles().add(userLeagueRole);
    }
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUsernameExists() {
        when(userRepository.findByUsernameOrEmail(MOCK_USERNAME, MOCK_USERNAME)).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(MOCK_USERNAME);
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals(testUser.getId(), customUserDetails.getId());
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
    }
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenEmailExists() {
        when(userRepository.findByUsernameOrEmail(MOCK_EMAIL, MOCK_EMAIL)).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(MOCK_EMAIL);
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        assertEquals(testUser.getEmail(), testUser.getEmail());
    }
    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsernameOrEmail("nonexistentuser", "nonexistentuser")).thenReturn(Optional.empty());
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistentuser");
        });
        String expectedMessage = "User not found with username or email: nonexistentuser";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUsernameHasDifferentCase() {
        String inputUsername = "TestUser";
        when(userRepository.findByUsernameOrEmail(inputUsername, inputUsername)).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(inputUsername);
        assertNotNull(userDetails);
        assertEquals(MOCK_USERNAME, userDetails.getUsername());
    }
    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithCorrectRoles() {
        when(userRepository.findByUsernameOrEmail(MOCK_USERNAME, MOCK_USERNAME)).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(MOCK_USERNAME);
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        assertTrue(roles.contains("ROLE_ADMIN"));
    }
}