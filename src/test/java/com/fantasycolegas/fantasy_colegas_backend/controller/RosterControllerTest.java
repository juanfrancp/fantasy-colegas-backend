package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.config.SecurityConfiguration;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.AddPlayerToRosterDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterPlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.service.RosterService;
import com.fantasycolegas.fantasy_colegas_backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RosterController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class, RosterControllerTest.TestConfig.class})
class RosterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RosterService rosterService; // Spring inyectará el mock de nuestra TestConfig aquí

    private final long MOCK_LEAGUE_ID = 1L;
    private final long MOCK_USER_ID = 1L;
    private final long MOCK_PLAYER_ID = 10L;

    @BeforeEach
    void resetMocks() {
        reset(rosterService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RosterService rosterService() {
            return mock(RosterService.class);
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
    @WithTestUser(id = MOCK_USER_ID)
    void createRoster_whenAuthenticated_shouldReturnOk() throws Exception {
        RosterCreateDto createDto = new RosterCreateDto();
        createDto.setPlayers(new ArrayList<>());

        when(rosterService.createRoster(eq(MOCK_LEAGUE_ID), any(RosterCreateDto.class), eq(MOCK_USER_ID)))
                .thenReturn("Equipo creado con éxito");

        mockMvc.perform(post("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserRoster_whenAuthenticated_shouldReturnRoster() throws Exception {
        List<RosterPlayerResponseDto> roster = new ArrayList<>();
        roster.add(new RosterPlayerResponseDto(MOCK_PLAYER_ID, "Test Player", PlayerTeamRole.CAMPO, "image.png", 100));

        when(rosterService.getUserRoster(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(roster);

        mockMvc.perform(get("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerId", is((int) MOCK_PLAYER_ID)));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void removePlayerFromRoster_whenAuthenticated_shouldReturnOk() throws Exception {
        when(rosterService.removePlayerFromRoster(MOCK_LEAGUE_ID, MOCK_USER_ID, MOCK_PLAYER_ID))
                .thenReturn("Jugador eliminado");

        mockMvc.perform(delete("/api/leagues/{leagueId}/rosters/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void addPlayerToRoster_whenAuthenticated_shouldReturnOk() throws Exception {
        AddPlayerToRosterDto addDto = new AddPlayerToRosterDto();
        addDto.setPlayerId(MOCK_PLAYER_ID);
        addDto.setPosition(PlayerTeamRole.CAMPO);

        when(rosterService.addPlayerToRoster(eq(MOCK_LEAGUE_ID), eq(MOCK_USER_ID), eq(MOCK_PLAYER_ID), any(PlayerTeamRole.class)))
                .thenReturn("Jugador añadido");

        mockMvc.perform(put("/api/leagues/{leagueId}/rosters/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createRoster_whenServiceThrowsException_shouldReturnCorrectStatus() throws Exception {
        RosterCreateDto createDto = new RosterCreateDto();
        createDto.setPlayers(new ArrayList<>());

        when(rosterService.createRoster(eq(MOCK_LEAGUE_ID), any(RosterCreateDto.class), eq(MOCK_USER_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos"));

        mockMvc.perform(post("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void removePlayerFromRoster_whenServiceThrowsException_shouldReturnCorrectStatus() throws Exception {
        when(rosterService.removePlayerFromRoster(MOCK_LEAGUE_ID, MOCK_USER_ID, MOCK_PLAYER_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar al jugador"));

        mockMvc.perform(delete("/api/leagues/{leagueId}/rosters/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void addPlayerToRoster_whenServiceThrowsException_shouldReturnCorrectStatus() throws Exception {
        AddPlayerToRosterDto addDto = new AddPlayerToRosterDto();
        addDto.setPlayerId(MOCK_PLAYER_ID);
        addDto.setPosition(PlayerTeamRole.CAMPO);

        when(rosterService.addPlayerToRoster(eq(MOCK_LEAGUE_ID), eq(MOCK_USER_ID), eq(MOCK_PLAYER_ID), any(PlayerTeamRole.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "El jugador ya está en el equipo"));

        mockMvc.perform(put("/api/leagues/{leagueId}/rosters/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createRoster_withInvalidData_shouldReturnBadRequest() throws Exception {
        RosterCreateDto createDto = new RosterCreateDto();

        mockMvc.perform(post("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void addPlayerToRoster_withInvalidData_shouldReturnBadRequest() throws Exception {
        AddPlayerToRosterDto addDto = new AddPlayerToRosterDto();

        mockMvc.perform(put("/api/leagues/{leagueId}/rosters/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void getUserRoster_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void removePlayerFromRoster_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/leagues/{leagueId}/rosters/players/{playerId}", MOCK_LEAGUE_ID, MOCK_PLAYER_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void addPlayerToRoster_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        AddPlayerToRosterDto addDto = new AddPlayerToRosterDto();
        addDto.setPlayerId(MOCK_PLAYER_ID);
        addDto.setPosition(PlayerTeamRole.CAMPO);

        mockMvc.perform(put("/api/leagues/{leagueId}/rosters/players", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void createRoster_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        RosterCreateDto createDto = new RosterCreateDto();
        createDto.setPlayers(new ArrayList<>());

        mockMvc.perform(post("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserRoster_whenServiceThrowsException_shouldReturnCorrectStatus() throws Exception {
        when(rosterService.getUserRoster(MOCK_LEAGUE_ID, MOCK_USER_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Roster no encontrado"));

        mockMvc.perform(get("/api/leagues/{leagueId}/rosters", MOCK_LEAGUE_ID))
                .andExpect(status().isNotFound());
    }

}