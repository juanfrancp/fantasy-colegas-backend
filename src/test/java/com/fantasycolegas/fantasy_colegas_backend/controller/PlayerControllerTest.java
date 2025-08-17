package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.config.SecurityConfiguration;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PointsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.service.PlayerService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class, PlayerControllerTest.TestConfig.class})
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerService playerService;

    private final long MOCK_LEAGUE_ID = 1L;
    private final long MOCK_PLAYER_ID = 1L;
    private final long MOCK_USER_ID = 1L;

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary public PlayerService playerService() { return mock(PlayerService.class); }
        @Bean @Primary public CustomUserDetailsService customUserDetailsService() { return mock(CustomUserDetailsService.class); }
        @Bean @Primary public JwtUtil jwtUtil() { return mock(JwtUtil.class); }
        @Bean @Primary public UserRepository userRepository() { return mock(UserRepository.class); }
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
            long userId = annotation.id();
            String username = annotation.username();
            Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            CustomUserDetails userDetails = new CustomUserDetails(userId, username, "password", authorities);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password", authorities);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            return context;
        }
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createPlayer_whenAuthenticated_shouldReturnCreated() throws Exception {
        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("New Player");
        PlayerResponseDto responseDto = new PlayerResponseDto(MOCK_PLAYER_ID, "New Player", null, 0);

        when(playerService.createPlayer(eq(MOCK_LEAGUE_ID), any(PlayerCreateDto.class), eq(MOCK_USER_ID)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/leagues/{leagueId}/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Player")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createPlayer_withInvalidData_shouldReturnBadRequest() throws Exception {
        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("");

        mockMvc.perform(post("/api/leagues/{leagueId}/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePlayer_whenAuthenticated_shouldReturnOk() throws Exception {
        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("Updated Name");
        PlayerResponseDto responseDto = new PlayerResponseDto(MOCK_PLAYER_ID, "Updated Name", null, 0);

        when(playerService.updatePlayer(eq(MOCK_LEAGUE_ID), eq(MOCK_PLAYER_ID), any(PlayerUpdateDto.class), eq(MOCK_USER_ID)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deletePlayer_whenAuthenticated_shouldReturnNoContent() throws Exception {
        doNothing().when(playerService).deletePlayer(MOCK_LEAGUE_ID, MOCK_PLAYER_ID, MOCK_USER_ID);

        mockMvc.perform(delete("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getPlayerById_shouldReturnPlayer() throws Exception {
        PlayerResponseDto responseDto = new PlayerResponseDto(MOCK_PLAYER_ID, "Test Player", null, 10);
        when(playerService.getPlayerById(MOCK_LEAGUE_ID, MOCK_PLAYER_ID)).thenReturn(responseDto);

        mockMvc.perform(get("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) MOCK_PLAYER_ID)));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePlayerPoints_whenAuthenticated_shouldReturnOk() throws Exception {
        PointsUpdateDto pointsDto = new PointsUpdateDto(100);
        PlayerResponseDto responseDto = new PlayerResponseDto(MOCK_PLAYER_ID, "Test Player", null, 100);

        when(playerService.updatePlayerPoints(eq(MOCK_LEAGUE_ID), eq(MOCK_PLAYER_ID), any(PointsUpdateDto.class), eq(MOCK_USER_ID)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/leagues/{leagueId}/players/{playerId}/points", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pointsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints", is(100)));
    }

    @Test
    void createPlayer_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("New Player");

        mockMvc.perform(post("/api/leagues/{leagueId}/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createPlayer_whenServiceThrowsException_shouldReturnCorrectStatus() throws Exception {
        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("New Player");

        when(playerService.createPlayer(eq(MOCK_LEAGUE_ID), any(PlayerCreateDto.class), eq(MOCK_USER_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos"));

        mockMvc.perform(post("/api/leagues/{leagueId}/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createPlayer_whenAuthenticatedButNotAdmin_shouldReturnForbidden() throws Exception {
        PlayerCreateDto createDto = new PlayerCreateDto();
        createDto.setName("Player by non-admin");

        when(playerService.createPlayer(eq(MOCK_LEAGUE_ID), any(PlayerCreateDto.class), eq(MOCK_USER_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador"));

        mockMvc.perform(post("/api/leagues/{leagueId}/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getPlayerById_withInvalidLeagueIdFormat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/leagues/{leagueId}/players/{playerId}", "invalid-id", MOCK_PLAYER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getPlayerById_whenPlayerNotFoundInService_shouldReturnNotFound() throws Exception {
        when(playerService.getPlayerById(MOCK_LEAGUE_ID, MOCK_PLAYER_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        mockMvc.perform(get("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePlayerPoints_whenAuthenticatedButNotAdmin_shouldReturnForbidden() throws Exception {
        PointsUpdateDto pointsDto = new PointsUpdateDto(100);

        when(playerService.updatePlayerPoints(eq(MOCK_LEAGUE_ID), eq(MOCK_PLAYER_ID), any(PointsUpdateDto.class), eq(MOCK_USER_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado"));

        mockMvc.perform(patch("/api/leagues/{leagueId}/players/{playerId}/points", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pointsDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getPlayerById_withInvalidPlayerIdFormat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, "invalid-player-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deletePlayer_whenAuthenticatedButNotAdmin_shouldReturnForbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador"))
                .when(playerService).deletePlayer(eq(MOCK_LEAGUE_ID), eq(MOCK_PLAYER_ID), eq(MOCK_USER_ID));

        mockMvc.perform(delete("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updatePlayer_whenAuthenticatedButNotAdmin_shouldReturnForbidden() throws Exception {
        PlayerUpdateDto updateDto = new PlayerUpdateDto();
        updateDto.setName("Attempted Update");

        when(playerService.updatePlayer(eq(MOCK_LEAGUE_ID), eq(MOCK_PLAYER_ID), any(PlayerUpdateDto.class), eq(MOCK_USER_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado"));

        mockMvc.perform(patch("/api/leagues/{leagueId}/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}