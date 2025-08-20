package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.config.SecurityConfiguration;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.ChangeRoleDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.JoinLeagueDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.LeagueTeamSizeUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.LeagueResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.UserScoreDto;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.UserRepository;
import com.fantasycolegas.fantasy_colegas_backend.security.CustomUserDetails;
import com.fantasycolegas.fantasy_colegas_backend.service.CustomUserDetailsService;
import com.fantasycolegas.fantasy_colegas_backend.service.LeagueService;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeagueController.class)
@Import({SecurityConfiguration.class, GlobalExceptionHandler.class})
public class LeagueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeagueService leagueService;

    private final long MOCK_LEAGUE_ID = 1L;
    private final long MOCK_USER_ID = 1L;
    private final long TARGET_USER_ID = 2L;
    private final long REQUEST_ID = 1L;


    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public LeagueService leagueService() {
            return mock(LeagueService.class);
        }

        @Bean
        @Primary
        public CustomUserDetailsService customUserDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean
        @Primary
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        @Primary
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = LeagueControllerTest.TestUserSecurityContextFactory.class)
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
    void getLeagueScoreboard_shouldReturnScoreboard_whenLeagueExists() throws Exception {
        List<UserScoreDto> scoreboard = new ArrayList<>();
        scoreboard.add(new UserScoreDto(1L, "userOne", 100.0));
        scoreboard.add(new UserScoreDto(2L, "userTwo", 90.0));

        when(leagueService.getLeagueScoreboard(MOCK_LEAGUE_ID)).thenReturn(scoreboard);

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID + "/scoreboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].totalPoints", is(100.0)))
                .andExpect(jsonPath("$[1].userId", is(2)))
                .andExpect(jsonPath("$[0].username", is("userOne")))
                .andExpect(jsonPath("$[1].totalPoints", is(90.0)));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getLeagueById_shouldReturnLeague_whenUserIsMember() throws Exception {
        LeagueResponseDto leagueResponseDto = new LeagueResponseDto();
        leagueResponseDto.setId(MOCK_LEAGUE_ID);
        leagueResponseDto.setName("Test League");

        when(leagueService.getLeagueById(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(leagueResponseDto);

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is((int) MOCK_LEAGUE_ID))).andExpect(jsonPath("$.name", is("Test League")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createLeague_shouldReturnCreatedLeague_whenAuthenticated() throws Exception {
        LeagueCreateDto leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("New League");
        leagueCreateDto.setTeamSize(5);

        LeagueResponseDto leagueResponseDto = new LeagueResponseDto();
        leagueResponseDto.setId(2L);
        leagueResponseDto.setName("New League");

        when(leagueService.createLeague(any(LeagueCreateDto.class), eq(MOCK_USER_ID))).thenReturn(leagueResponseDto);

        mockMvc.perform(post("/api/leagues").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(leagueCreateDto))).andExpect(status().isCreated()).andExpect(jsonPath("$.name", is("New League")));
    }

    @Test
    void createLeague_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        LeagueCreateDto leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("New League");

        mockMvc.perform(post("/api/leagues").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(leagueCreateDto))).andExpect(status().isUnauthorized());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void joinLeague_shouldReturnJoinedLeague_whenCodeIsValid() throws Exception {
        JoinLeagueDto joinLeagueDto = new JoinLeagueDto();
        joinLeagueDto.setJoinCode("VALIDCODE");

        LeagueResponseDto leagueResponseDto = new LeagueResponseDto();
        leagueResponseDto.setId(MOCK_LEAGUE_ID);
        leagueResponseDto.setName("Joined League");

        when(leagueService.joinLeague(eq("VALIDCODE"), eq(MOCK_USER_ID))).thenReturn(leagueResponseDto);

        mockMvc.perform(post("/api/leagues/join").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(joinLeagueDto))).andExpect(status().isOk()).andExpect(jsonPath("$.name", is("Joined League")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void joinLeague_shouldReturnBadRequest_whenCodeIsInvalid() throws Exception {
        JoinLeagueDto joinLeagueDto = new JoinLeagueDto();
        joinLeagueDto.setJoinCode("INVALIDCODE");

        when(leagueService.joinLeague(eq("INVALIDCODE"), eq(MOCK_USER_ID))).thenThrow(new ResponseStatusException(BAD_REQUEST, "Invalid join code"));

        mockMvc.perform(post("/api/leagues/join").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(joinLeagueDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateTeamSize_shouldReturnOk_whenUserIsAdmin() throws Exception {
        LeagueTeamSizeUpdateDto updateDto = new LeagueTeamSizeUpdateDto();
        updateDto.setTeamSize(10);

        LeagueResponseDto leagueResponseDto = new LeagueResponseDto();
        leagueResponseDto.setId(MOCK_LEAGUE_ID);
        leagueResponseDto.setTeamSize(10);

        when(leagueService.updateLeagueTeamSize(eq(MOCK_LEAGUE_ID), any(LeagueTeamSizeUpdateDto.class), eq(MOCK_USER_ID))).thenReturn(leagueResponseDto);

        mockMvc.perform(patch("/api/leagues/" + MOCK_LEAGUE_ID + "/team-size").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.teamSize", is(10)));
    }

    @Test
    @WithTestUser(id = 2L)
    void updateTeamSize_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        LeagueTeamSizeUpdateDto updateDto = new LeagueTeamSizeUpdateDto();
        updateDto.setTeamSize(10);

        when(leagueService.updateLeagueTeamSize(eq(MOCK_LEAGUE_ID), any(LeagueTeamSizeUpdateDto.class), eq(2L))).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/api/leagues/" + MOCK_LEAGUE_ID + "/team-size").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void deleteLeague_shouldReturnNoContent_whenUserIsAdmin() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);

        doNothing().when(leagueService).deleteLeague(MOCK_LEAGUE_ID, MOCK_USER_ID);

        mockMvc.perform(delete("/api/leagues/" + MOCK_LEAGUE_ID).with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @WithTestUser(id = 2L)
    void deleteLeague_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/leagues/" + MOCK_LEAGUE_ID).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void changeUserRole_shouldReturnOk_whenAdminChangesRole() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doNothing().when(leagueService).changeUserRole(anyLong(), anyLong(), anyLong(), any());

        ChangeRoleDto changeRoleDto = new ChangeRoleDto();
        changeRoleDto.setNewRole(LeagueRole.ADMIN);

        mockMvc.perform(patch("/api/leagues/" + MOCK_LEAGUE_ID + "/participants/" + TARGET_USER_ID + "/role").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(changeRoleDto))).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void expelUser_shouldReturnNoContent_whenAdminExpelsUser() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doNothing().when(leagueService).expelUser(MOCK_LEAGUE_ID, MOCK_USER_ID, TARGET_USER_ID);

        mockMvc.perform(delete("/api/leagues/" + MOCK_LEAGUE_ID + "/expel/" + TARGET_USER_ID).with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void leaveLeague_shouldReturnNoContent_whenUserLeaves() throws Exception {
        doNothing().when(leagueService).leaveLeague(MOCK_LEAGUE_ID, MOCK_USER_ID);

        mockMvc.perform(delete("/api/leagues/" + MOCK_LEAGUE_ID + "/leave").with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void sendJoinRequest_shouldReturnOk() throws Exception {
        doNothing().when(leagueService).sendJoinRequest(MOCK_LEAGUE_ID, MOCK_USER_ID);

        mockMvc.perform(post("/api/leagues/" + MOCK_LEAGUE_ID + "/request-join").with(csrf())).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getPendingJoinRequests_shouldReturnRequests_whenUserIsAdmin() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        when(leagueService.getPendingJoinRequests(MOCK_LEAGUE_ID)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID + "/requests")).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void acceptJoinRequest_shouldReturnOk_whenUserIsAdmin() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doNothing().when(leagueService).acceptJoinRequest(REQUEST_ID);

        mockMvc.perform(post("/api/leagues/" + MOCK_LEAGUE_ID + "/requests/" + REQUEST_ID + "/accept").with(csrf())).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void rejectJoinRequest_shouldReturnOk_whenUserIsAdmin() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doNothing().when(leagueService).rejectJoinRequest(REQUEST_ID);

        mockMvc.perform(post("/api/leagues/" + MOCK_LEAGUE_ID + "/requests/" + REQUEST_ID + "/reject").with(csrf())).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createLeague_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        LeagueCreateDto leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("");
        leagueCreateDto.setTeamSize(5);

        mockMvc.perform(post("/api/leagues").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(leagueCreateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createLeague_shouldReturnBadRequest_whenTeamSizeIsOutOfRange() throws Exception {
        LeagueCreateDto leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("Test League");
        leagueCreateDto.setTeamSize(20);

        mockMvc.perform(post("/api/leagues").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(leagueCreateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = 3L)
    void getLeagueById_shouldReturnForbidden_whenUserIsNotMember() throws Exception {
        when(leagueService.getLeagueById(MOCK_LEAGUE_ID, 3L)).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID)).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void changeUserRole_shouldReturnBadRequest_whenAdminChangesOwnRole() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.")).when(leagueService).changeUserRole(MOCK_LEAGUE_ID, MOCK_USER_ID, MOCK_USER_ID, LeagueRole.PARTICIPANT);

        ChangeRoleDto changeRoleDto = new ChangeRoleDto();
        changeRoleDto.setNewRole(LeagueRole.PARTICIPANT);

        mockMvc.perform(patch("/api/leagues/" + MOCK_LEAGUE_ID + "/participants/" + MOCK_USER_ID + "/role").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(changeRoleDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void expelUser_shouldReturnBadRequest_whenAdminExpelsSelf() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes expulsarte a ti mismo.")).when(leagueService).expelUser(MOCK_LEAGUE_ID, MOCK_USER_ID, MOCK_USER_ID);

        mockMvc.perform(delete("/api/leagues/" + MOCK_LEAGUE_ID + "/expel/" + MOCK_USER_ID).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void sendJoinRequest_shouldReturnConflict_whenRequestIsAlreadyPending() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud pendiente.")).when(leagueService).sendJoinRequest(MOCK_LEAGUE_ID, MOCK_USER_ID);

        mockMvc.perform(post("/api/leagues/" + MOCK_LEAGUE_ID + "/request-join").with(csrf())).andExpect(status().isConflict());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getRosterByTeamId_shouldReturnRoster_whenUserIsMember() throws Exception {
        when(leagueService.getRosterByTeamId(eq(MOCK_LEAGUE_ID), eq(TARGET_USER_ID), anyString())).thenReturn(new RosterResponseDto());

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID + "/rosters/" + TARGET_USER_ID)).andExpect(status().isOk());
    }

    @Test
    @WithTestUser(id = 3L)
    void getRosterByTeamId_shouldReturnForbidden_whenUserIsNotMember() throws Exception {
        when(leagueService.getRosterByTeamId(eq(MOCK_LEAGUE_ID), eq(TARGET_USER_ID), anyString())).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID + "/rosters/" + TARGET_USER_ID)).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getUserPointsInLeague_shouldReturnUserPoints() throws Exception {
        UserScoreDto userScoreDto = new UserScoreDto(TARGET_USER_ID, "targetUser", 150.0);

        when(leagueService.getUserPointsInLeague(MOCK_LEAGUE_ID, TARGET_USER_ID)).thenReturn(userScoreDto);

        mockMvc.perform(get("/api/leagues/" + MOCK_LEAGUE_ID + "/users/" + TARGET_USER_ID + "/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is((int) TARGET_USER_ID)))
                .andExpect(jsonPath("$.username", is("targetUser")))
                .andExpect(jsonPath("$.totalPoints", is(150.0)));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void leaveLeague_shouldReturnBadRequest_whenLastAdminTriesToLeave() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes abandonar la liga siendo el único administrador")).when(leagueService).leaveLeague(MOCK_LEAGUE_ID, MOCK_USER_ID);

        mockMvc.perform(delete("/api/leagues/" + MOCK_LEAGUE_ID + "/leave").with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void createLeague_shouldReturnBadRequest_whenTeamSizeIsBelowMinimum() throws Exception {
        LeagueCreateDto leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("Test League");
        leagueCreateDto.setTeamSize(2);

        mockMvc.perform(post("/api/leagues").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(leagueCreateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateLeague_shouldReturnBadRequest_whenDataIsInvalid() throws Exception {
        LeagueCreateDto leagueCreateDto = new LeagueCreateDto();
        leagueCreateDto.setName("");
        leagueCreateDto.setTeamSize(5);

        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);

        mockMvc.perform(put("/api/leagues/" + MOCK_LEAGUE_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(leagueCreateDto))).andExpect(status().isBadRequest());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void updateLeague_shouldReturnOk_whenAdminUpdatesLeague() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);

        LeagueCreateDto updateDto = new LeagueCreateDto("Nombre Actualizado", "Desc Actualizada", null, false, 10, 5);
        LeagueResponseDto responseDto = new LeagueResponseDto();
        responseDto.setId(MOCK_LEAGUE_ID);
        responseDto.setName("Nombre Actualizado");

        when(leagueService.updateLeague(eq(MOCK_LEAGUE_ID), any(LeagueCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/leagues/" + MOCK_LEAGUE_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk()).andExpect(jsonPath("$.name", is("Nombre Actualizado")));
    }

    @Test
    @WithTestUser(id = 2L)
    void acceptJoinRequest_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, 2L)).thenReturn(false);

        mockMvc.perform(post("/api/leagues/" + MOCK_LEAGUE_ID + "/requests/" + REQUEST_ID + "/accept").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void changeUserRole_shouldReturnNotFound_whenTargetUserIsNotInLeague() throws Exception {
        when(leagueService.checkIfUserIsAdmin(MOCK_LEAGUE_ID, MOCK_USER_ID)).thenReturn(true);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no es miembro de esta liga.")).when(leagueService).changeUserRole(MOCK_LEAGUE_ID, MOCK_USER_ID, 99L, LeagueRole.ADMIN);

        ChangeRoleDto changeRoleDto = new ChangeRoleDto();
        changeRoleDto.setNewRole(LeagueRole.ADMIN);

        mockMvc.perform(patch("/api/leagues/" + MOCK_LEAGUE_ID + "/participants/99/role").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(changeRoleDto))).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getMyLeagues_shouldReturnLeaguesForAuthenticatedUser() throws Exception {
        LeagueResponseDto league1 = new LeagueResponseDto();
        league1.setId(1L);
        league1.setName("Mi Liga 1");

        LeagueResponseDto league2 = new LeagueResponseDto();
        league2.setId(2L);
        league2.setName("Mi Liga 2");

        when(leagueService.getLeaguesByUserId(MOCK_USER_ID)).thenReturn(List.of(league1, league2));

        mockMvc.perform(get("/api/leagues/my-leagues").with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.length()", is(2))).andExpect(jsonPath("$[0].name", is("Mi Liga 1"))).andExpect(jsonPath("$[1].name", is("Mi Liga 2")));
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getMyLeagues_whenServiceThrowsNotFound_shouldReturnNotFoundStatus() throws Exception {
        when(leagueService.getLeaguesByUserId(MOCK_USER_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(get("/api/leagues/my-leagues").with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getPublicLeagues_shouldReturnListOfPublicLeagues() throws Exception {
        LeagueResponseDto publicLeague = new LeagueResponseDto();
        publicLeague.setId(10L);
        publicLeague.setName("Liga Pública de Verano");
        when(leagueService.getPublicLeagues()).thenReturn(List.of(publicLeague));

        mockMvc.perform(get("/api/leagues/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Liga Pública de Verano")));

        verify(leagueService, times(1)).getPublicLeagues();
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void searchLeaguesByName_whenNameProvided_shouldReturnMatchingLeagues() throws Exception {
        String searchTerm = "MiLiga";
        LeagueResponseDto foundLeague = new LeagueResponseDto();
        foundLeague.setId(11L);
        foundLeague.setName("MiLiga de Fantasía");
        when(leagueService.searchLeaguesByName(searchTerm)).thenReturn(List.of(foundLeague));

        mockMvc.perform(get("/api/leagues/search/name")
                        .param("name", searchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("MiLiga de Fantasía")));

        verify(leagueService, times(1)).searchLeaguesByName(searchTerm);
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void searchLeaguesByName_whenNameIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/leagues/search/name"))
                .andExpect(status().isBadRequest());
        verify(leagueService, never()).searchLeaguesByName(anyString());
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getLeagueByJoinCode_whenCodeExists_shouldReturnLeague() throws Exception {
        String joinCode = "CODE123";
        LeagueResponseDto foundLeague = new LeagueResponseDto();
        foundLeague.setId(12L);
        foundLeague.setName("Liga por Código");
        when(leagueService.getLeagueByJoinCode(joinCode)).thenReturn(foundLeague);

        mockMvc.perform(get("/api/leagues/search/code")
                        .param("code", joinCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(12)))
                .andExpect(jsonPath("$.name", is("Liga por Código")));

        verify(leagueService, times(1)).getLeagueByJoinCode(joinCode);
    }

    @Test
    @WithTestUser(id = MOCK_USER_ID)
    void getLeagueByJoinCode_whenCodeNotFound_shouldReturnNotFound() throws Exception {
        String invalidCode = "NONEXISTENT";
        when(leagueService.getLeagueByJoinCode(invalidCode))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada"));

        mockMvc.perform(get("/api/leagues/search/code")
                        .param("code", invalidCode))
                .andExpect(status().isNotFound());

        verify(leagueService, times(1)).getLeagueByJoinCode(invalidCode);
    }
}