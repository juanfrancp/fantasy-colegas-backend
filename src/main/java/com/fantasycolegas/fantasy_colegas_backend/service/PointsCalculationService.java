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

    public double calculateTotalPoints(PlayerMatchStatsUpdateDto statsDto, PlayerTeamRole playerRole) {
        // Obtenemos todas las reglas de puntuación para el rol del jugador (CAMPO o PORTERO)
        List<ScoringRule> rules = scoringRuleRepository.findAllByRole(playerRole);
        double totalPoints = 0.0;

        for (ScoringRule rule : rules) {
            double pointsToAdd = getStatValue(statsDto, rule.getStatName()) * rule.getPointsPerUnit();
            totalPoints += pointsToAdd;
        }

        return totalPoints;
    }

    private int getStatValue(PlayerMatchStatsUpdateDto statsDto, String statName) {
        // Aquí necesitas mapear el nombre de la estadística del DTO con los getters
        // Esto es un ejemplo, debes asegurar que los nombres coincidan
        switch (statName) {
            case "golesMarcados":
                return statsDto.getGolesMarcados();
            case "asistencias":
                return statsDto.getAsistencias();
            case "fallosClarosDeGol":
                return statsDto.getFallosClarosDeGol();
            // ... Agrega todos los casos para las demás estadísticas
            case "tarjetasAmarillas":
                return statsDto.getTarjetasAmarillas();
            case "tarjetasRojas":
                return statsDto.getTarjetasRojas();
            default:
                return 0; // Si la estadística no está en la lista, no se añaden puntos
        }
    }
}