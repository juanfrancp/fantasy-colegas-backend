package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.config.SecurityConfiguration;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerMatchStatsResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.service.LeagueService;
import com.fantasycolegas.fantasy_colegas_backend.service.MatchService;
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
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class})
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatchService matchService;

    @Autowired
    private LeagueService leagueService;

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary public MatchService matchService() { return mock(MatchService.class); }
        @Bean @Primary public LeagueService leagueService() { return mock(LeagueService.class); }
        @Bean @Primary public CustomUserDetailsService customUserDetailsService() { return mock(CustomUserDetailsService.class); }
        @Bean @Primary public JwtUtil jwtUtil() { return mock(JwtUtil.class); }
        @Bean @Primary public UserRepository userRepository() { return mock(UserRepository.class); }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = TestUserSecurityContextFactory.class)
    public @interface WithTestUser {
        long id() default 1L;
    }

    static class TestUserSecurityContextFactory implements WithSecurityContextFactory<WithTestUser> {
        @Override
        public SecurityContext createSecurityContext(WithTestUser annotation) {
            var userDetails = new CustomUserDetails(annotation.id(), "test-user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            return context;
        }
    }

    @Test
    @WithTestUser
    void createMatch_whenUserIsAdmin_shouldReturnCreated() throws Exception {
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(1L);
        createDto.setMatchDate(LocalDate.now());

        when(leagueService.checkIfUserIsAdmin(eq(1L), any())).thenReturn(true);
        when(matchService.createMatch(any(MatchCreateDto.class)))
                .thenReturn(new MatchResponseDto(1L, "New Match", "Desc", LocalDate.now(), 1L, "League Name"));

        mockMvc.perform(post("/api/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Match"));
    }

    @Test
    @WithTestUser
    void createMatch_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(1L);
        createDto.setMatchDate(LocalDate.now());

        when(leagueService.checkIfUserIsAdmin(eq(1L), any())).thenReturn(false);

        mockMvc.perform(post("/api/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMatch_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(1L);
        createDto.setMatchDate(LocalDate.now());

        mockMvc.perform(post("/api/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser
    void createMatch_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"leagueId\": 1, \"matchDate: \"2025-08-18\"}";

        when(leagueService.checkIfUserIsAdmin(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser
    void updatePlayerStats_whenUserIsAdmin_shouldReturnOk() throws Exception {
        long matchId = 1L;
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(1L);

        when(matchService.checkIfUserIsAdminOfMatchLeague(eq(matchId), any())).thenReturn(true);
        when(matchService.updatePlayerStats(eq(matchId), any(PlayerMatchStatsUpdateDto.class)))
                .thenReturn(new PlayerMatchStatsResponseDto());

        mockMvc.perform(patch("/api/matches/{matchId}/stats", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithTestUser
    void updatePlayerStats_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        long matchId = 1L;
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(1L);

        when(matchService.checkIfUserIsAdminOfMatchLeague(eq(matchId), any())).thenReturn(false);

        mockMvc.perform(patch("/api/matches/{matchId}/stats", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser
    void updatePlayerStats_withInvalidData_shouldReturnBadRequest() throws Exception {
        long matchId = 1L;
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(1L);
        updateDto.setGolesMarcados(-1);

        when(matchService.checkIfUserIsAdminOfMatchLeague(eq(matchId), any())).thenReturn(true);

        mockMvc.perform(patch("/api/matches/{matchId}/stats", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser
    void updatePlayerStats_whenServiceThrowsNotFound_shouldReturnNotFound() throws Exception {
        long matchId = 99L;
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(1L);

        when(matchService.checkIfUserIsAdminOfMatchLeague(eq(matchId), any())).thenReturn(true);
        when(matchService.updatePlayerStats(eq(matchId), any(PlayerMatchStatsUpdateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado."));

        mockMvc.perform(patch("/api/matches/{matchId}/stats", matchId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser
    void updatePlayerStats_withInvalidMatchIdType_shouldReturnBadRequest() throws Exception {
        PlayerMatchStatsUpdateDto updateDto = new PlayerMatchStatsUpdateDto();
        updateDto.setPlayerId(1L);

        mockMvc.perform(patch("/api/matches/{matchId}/stats", "invalid-id")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }
}