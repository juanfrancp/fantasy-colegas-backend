package com.fantasycolegas.fantasy_colegas_backend.controller;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.MatchCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.MatchResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.service.MatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Construimos MockMvc manualmente para testear el controlador en aislamiento
        mockMvc = MockMvcBuilders.standaloneSetup(matchController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Opcional: para probar el manejo de excepciones
                .build();

        // Configuramos un ObjectMapper que entienda tipos de Java 8 como LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createMatch_withValidData_shouldReturnCreated() throws Exception {
        // Arrange
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setLeagueId(1L);
        createDto.setMatchDate(LocalDateTime.now().plusDays(1));
        createDto.setHomeTeamName("Home");
        createDto.setAwayTeamName("Away");
        createDto.setHomeTeamPlayerIds(List.of(1L));
        createDto.setAwayTeamPlayerIds(List.of(2L));

        when(matchService.createMatch(any(MatchCreateDto.class)))
                .thenReturn(new MatchResponseDto(1L, null, null, null, null, createDto.getMatchDate()));

        // Act & Assert
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createMatch_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange: DTO sin un campo requerido como leagueId
        MatchCreateDto createDto = new MatchCreateDto();
        createDto.setMatchDate(LocalDateTime.now().plusDays(1)); // Fecha en futuro para pasar esa validación

        // Act & Assert
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUpcomingMatches_shouldReturnOkAndListOfMatches() throws Exception {
        // Arrange
        MatchResponseDto upcomingMatch = new MatchResponseDto(1L, null, null, 0, 0, LocalDateTime.now().plusDays(1));
        when(matchService.getUpcomingMatches()).thenReturn(List.of(upcomingMatch));

        // Act & Assert
        mockMvc.perform(get("/api/matches/upcoming")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getPastMatches_shouldReturnOkAndListOfMatches() throws Exception {
        // Arrange
        MatchResponseDto pastMatch = new MatchResponseDto(2L, null, null, 2, 1, LocalDateTime.now().minusDays(1));
        when(matchService.getPastMatches()).thenReturn(List.of(pastMatch));

        // Act & Assert
        mockMvc.perform(get("/api/matches/past")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }
}