package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.PlayerMatchStatsUpdateDto;
import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.ScoringRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Servicio para el cálculo de los puntos de los jugadores en un partido.
 * <p>
 * Este servicio se encarga de aplicar las reglas de puntuación definidas
 * para cada rol de jugador (portero o jugador de campo) a las estadísticas
 * de un partido para calcular los puntos totales.
 * </p>
 */
@Service
public class PointsCalculationService {

    private final ScoringRuleRepository scoringRuleRepository;

    /**
     * Constructor del servicio que inyecta el repositorio de reglas de puntuación.
     *
     * @param scoringRuleRepository El repositorio de reglas de puntuación.
     */
    public PointsCalculationService(ScoringRuleRepository scoringRuleRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
    }

    /**
     * Calcula los puntos de un jugador para un partido basándose en su rol.
     * <p>
     * Obtiene las reglas de puntuación para un rol específico y las aplica
     * a cada estadística del jugador para obtener una puntuación total.
     * </p>
     *
     * @param statsDto   DTO con las estadísticas del jugador para el partido.
     * @param playerRole El rol del jugador (ej. {@link PlayerTeamRole#CAMPO} o {@link PlayerTeamRole#PORTERO}).
     * @return Los puntos totales calculados para el jugador.
     */
    public double calculatePointsForRole(PlayerMatchStatsUpdateDto statsDto, PlayerTeamRole playerRole) {
        List<ScoringRule> rules = scoringRuleRepository.findAllByRole(playerRole);
        double totalPoints = 0.0;

        for (ScoringRule rule : rules) {
            double statValue = getStatValue(statsDto, rule.getStatName());
            totalPoints += statValue * rule.getPointsPerUnit();
        }

        return totalPoints;
    }

    /**
     * Método auxiliar para obtener el valor de una estadística del DTO.
     *
     * @param statsDto DTO con las estadísticas del jugador.
     * @param statName El nombre de la estadística.
     * @return El valor de la estadística o 0.0 si no se encuentra.
     */
    private double getStatValue(PlayerMatchStatsUpdateDto statsDto, String statName) {
        switch (statName) {
            case "golesMarcados":
                return statsDto.getGolesMarcados();
            case "fallosClarosDeGol":
                return statsDto.getFallosClarosDeGol();
            case "asistencias":
                return statsDto.getAsistencias();
            case "golesEncajadosComoPortero":
                return statsDto.getGolesEncajadosComoPortero();
            case "paradasComoPortero":
                return statsDto.getParadasComoPortero();
            case "cesionesConcedidas":
                return statsDto.getCesionesConcedidas();
            case "faltasCometidas":
                return statsDto.getFaltasCometidas();
            case "faltasRecibidas":
                return statsDto.getFaltasRecibidas();
            case "penaltisRecibidos":
                return statsDto.getPenaltisRecibidos();
            case "penaltisCometidos":
                return statsDto.getPenaltisCometidos();
            case "pasesAcertados":
                return statsDto.getPasesAcertados();
            case "pasesFallados":
                return statsDto.getPasesFallados();
            case "robosDeBalon":
                return statsDto.getRobosDeBalon();
            case "tirosCompletados":
                return statsDto.getTirosCompletados();
            case "tirosEntreLosTresPalos":
                return statsDto.getTirosEntreLosTresPalos();
            case "tiempoJugado":
                return statsDto.getTiempoJugado();
            case "tarjetasAmarillas":
                return statsDto.getTarjetasAmarillas();
            case "tarjetasRojas":
                return statsDto.getTarjetasRojas();
            default:
                return 0.0;
        }
    }
}