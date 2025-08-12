package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.ScoringRuleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PointsCalculationService {

    private final ScoringRuleRepository scoringRuleRepository;

    public PointsCalculationService(ScoringRuleRepository scoringRuleRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
    }

    public double calculatePointsForRole(PlayerMatchStatsUpdateDto statsDto, PlayerTeamRole playerRole) {
        List<ScoringRule> rules = scoringRuleRepository.findAllByRole(playerRole);
        double totalPoints = 0.0;

        for (ScoringRule rule : rules) {
            double statValue = getStatValue(statsDto, rule.getStatName());
            totalPoints += statValue * rule.getPointsPerUnit();
        }

        return totalPoints;
    }

    private double getStatValue(PlayerMatchStatsUpdateDto statsDto, String statName) {
        switch (statName) {
            case "golesMarcados": return statsDto.getGolesMarcados();
            case "fallosClarosDeGol": return statsDto.getFallosClarosDeGol();
            case "asistencias": return statsDto.getAsistencias();
            case "golesEncajadosComoPortero": return statsDto.getGolesEncajadosComoPortero();
            case "paradasComoPortero": return statsDto.getParadasComoPortero();
            case "cesionesConcedidas": return statsDto.getCesionesConcedidas();
            case "faltasCometidas": return statsDto.getFaltasCometidas();
            case "faltasRecibidas": return statsDto.getFaltasRecibidas();
            case "penaltisRecibidos": return statsDto.getPenaltisRecibidos();
            case "penaltisCometidos": return statsDto.getPenaltisCometidos();
            case "pasesAcertados": return statsDto.getPasesAcertados();
            case "pasesFallados": return statsDto.getPasesFallados();
            case "robosDeBalon": return statsDto.getRobosDeBalon();
            case "tirosCompletados": return statsDto.getTirosCompletados();
            case "tirosEntreLosTresPalos": return statsDto.getTirosEntreLosTresPalos();
            case "tiempoJugado": return statsDto.getTiempoJugado();
            case "tarjetasAmarillas": return statsDto.getTarjetasAmarillas();
            case "tarjetasRojas": return statsDto.getTarjetasRojas();
            default: return 0.0;
        }
    }
}