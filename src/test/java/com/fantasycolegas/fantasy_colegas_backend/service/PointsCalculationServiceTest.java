package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.ScoringRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsCalculationServiceTest {

    @Mock
    private ScoringRuleRepository scoringRuleRepository;

    @InjectMocks
    private PointsCalculationService pointsCalculationService;

    private List<ScoringRule> campoRules;
    private List<ScoringRule> porteroRules;

    @BeforeEach
    void setUp() {
        campoRules = new ArrayList<>();
        campoRules.add(createRule("golesMarcados", 5.0, PlayerTeamRole.CAMPO));
        campoRules.add(createRule("asistencias", 3.0, PlayerTeamRole.CAMPO));
        campoRules.add(createRule("tarjetasAmarillas", -1.0, PlayerTeamRole.CAMPO));

        porteroRules = new ArrayList<>();
        porteroRules.add(createRule("paradasComoPortero", 0.5, PlayerTeamRole.PORTERO));
        porteroRules.add(createRule("golesEncajadosComoPortero", -2.0, PlayerTeamRole.PORTERO));
    }

    private ScoringRule createRule(String statName, double points, PlayerTeamRole role) {
        ScoringRule rule = new ScoringRule();
        rule.setStatName(statName);
        rule.setPointsPerUnit(points);
        rule.setRole(role);
        return rule;
    }

    @Test
    void calculatePointsForRole_shouldCalculateCorrectPointsForCampoPlayer() {
        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();
        statsDto.setGolesMarcados(2);
        statsDto.setAsistencias(1);
        statsDto.setTarjetasAmarillas(1);

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.CAMPO)).thenReturn(campoRules);

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.CAMPO);

        assertEquals(12.0, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldCalculateCorrectPointsForPortero() {
        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();
        statsDto.setParadasComoPortero(5);
        statsDto.setGolesEncajadosComoPortero(2);

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.PORTERO)).thenReturn(porteroRules);

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.PORTERO);

        assertEquals(-1.5, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldReturnZeroWhenNoRelevantStats() {
        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();
        statsDto.setGolesMarcados(2);

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.PORTERO)).thenReturn(porteroRules);

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.PORTERO);

        assertEquals(0.0, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldReturnZeroWhenNoRulesExist() {
        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();
        statsDto.setGolesMarcados(1);

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.CAMPO)).thenReturn(Collections.emptyList());

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.CAMPO);

        assertEquals(0.0, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldHandleAllStatsFields() {
        List<ScoringRule> allRules = new ArrayList<>();
        allRules.add(createRule("golesMarcados", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("fallosClarosDeGol", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("asistencias", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("cesionesConcedidas", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("faltasCometidas", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("faltasRecibidas", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("penaltisRecibidos", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("penaltisCometidos", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("pasesAcertados", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("pasesFallados", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("robosDeBalon", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("tirosCompletados", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("tirosEntreLosTresPalos", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("tiempoJugado", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("tarjetasAmarillas", 1, PlayerTeamRole.CAMPO));
        allRules.add(createRule("tarjetasRojas", 1, PlayerTeamRole.CAMPO));

        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();
        statsDto.setGolesMarcados(1);
        statsDto.setFallosClarosDeGol(1);
        statsDto.setAsistencias(1);
        statsDto.setCesionesConcedidas(1);
        statsDto.setFaltasCometidas(1);
        statsDto.setFaltasRecibidas(1);
        statsDto.setPenaltisRecibidos(1);
        statsDto.setPenaltisCometidos(1);
        statsDto.setPasesAcertados(1);
        statsDto.setPasesFallados(1);
        statsDto.setRobosDeBalon(1);
        statsDto.setTirosCompletados(1);
        statsDto.setTirosEntreLosTresPalos(1);
        statsDto.setTiempoJugado(1);
        statsDto.setTarjetasAmarillas(1);
        statsDto.setTarjetasRojas(1);

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.CAMPO)).thenReturn(allRules);

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.CAMPO);
        assertEquals(16.0, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldReturnZeroWhenAllStatsAreZero() {
        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.CAMPO)).thenReturn(campoRules);

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.CAMPO);

        assertEquals(0.0, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldIgnoreInvalidStatNamesInRules() {
        PlayerMatchStatsUpdateDto statsDto = new PlayerMatchStatsUpdateDto();
        statsDto.setGolesMarcados(1);

        campoRules.add(createRule("estadisticaInvalida", 100.0, PlayerTeamRole.CAMPO));

        when(scoringRuleRepository.findAllByRole(PlayerTeamRole.CAMPO)).thenReturn(campoRules);

        double totalPoints = pointsCalculationService.calculatePointsForRole(statsDto, PlayerTeamRole.CAMPO);

        assertEquals(5.0, totalPoints, 0.001);
    }

    @Test
    void calculatePointsForRole_shouldThrowExceptionWhenDtoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            pointsCalculationService.calculatePointsForRole(null, PlayerTeamRole.CAMPO);
        });
    }
}