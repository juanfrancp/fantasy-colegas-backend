package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.model.PlayerMatchStats; // Importante: Usamos el Modelo/Entidad
import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.ScoringRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para el cálculo de los puntos.
 * Refactorizado para trabajar directamente con la entidad PlayerMatchStats.
 */
@Service
public class PointsCalculationService {

    private final ScoringRuleRepository scoringRuleRepository;

    public PointsCalculationService(ScoringRuleRepository scoringRuleRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
    }

    /**
     * Calcula los puntos usando la Entidad PlayerMatchStats (ya rellena con los datos del DTO en MatchService).
     */
    public double calculatePointsForRole(PlayerMatchStats stats, PlayerTeamRole playerRole) {
        if (stats == null) {
            throw new IllegalArgumentException("Las estadísticas no pueden ser nulas.");
        }

        List<ScoringRule> rules = scoringRuleRepository.findAllByRole(playerRole);
        double totalPoints = 0.0;

        for (ScoringRule rule : rules) {
            // Pasamos la Entidad, no el DTO
            double statValue = getStatValue(stats, rule.getStatName());
            totalPoints += statValue * rule.getPointsPerUnit();
        }

        return totalPoints;
    }

    /**
     * Extrae el valor numérico de la Entidad PlayerMatchStats.
     */
    private double getStatValue(PlayerMatchStats stats, String statName) {
        switch (statName) {
            // --- OFENSIVAS ---
            case "golesMarcados": return stats.getGolesMarcados();
            case "asistencias": return stats.getAsistencias();
            case "fallosClarosDeGol": return stats.getFallosClarosDeGol();

            // --- GENERALES ---
            case "faltasCometidas": return stats.getFaltasCometidas();
            case "faltasRecibidas": return stats.getFaltasRecibidas();
            case "penaltisCometidos": return stats.getPenaltisCometidos();
            case "penaltisRecibidos": return stats.getPenaltisRecibidos();
            case "tarjetasAmarillas": return stats.getTarjetasAmarillas();
            case "tarjetasRojas": return stats.getTarjetasRojas();
            case "salvadasDeGol": return stats.getSalvadasDeGol();

            // --- PORTERO ---
            case "paradasComoPortero": return stats.getParadasComoPortero();
            case "golesEncajadosComoPortero": return stats.getGolesEncajadosComoPortero();
            case "penaltisParados": return stats.getPenaltisParados();

            // CORRECCIÓN IMPORTANTE: Evaluar el booleano real
            case "porteriaImbatida":
                // Si es true devuelve 1.0 (para multiplicar por los puntos), si no 0.0
                return stats.isPorteriaImbatida() ? 1.0 : 0.0;

            default:
                return 0.0;
        }
    }
}