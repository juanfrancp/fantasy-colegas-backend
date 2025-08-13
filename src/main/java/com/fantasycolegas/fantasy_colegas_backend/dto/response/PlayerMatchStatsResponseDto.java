package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la respuesta de las estadísticas de un jugador en un partido.
 * <p>
 * Este objeto se utiliza para transferir todas las métricas registradas de un jugador
 * en un partido, así como los puntos totales calculados para los roles de jugador
 * de campo y portero.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMatchStatsResponseDto {
    private Long id;
    private Long playerId;

    private int golesMarcados;
    private int fallosClarosDeGol;
    private int asistencias;
    private int golesEncajadosComoPortero;
    private int paradasComoPortero;
    private int cesionesConcedidas;
    private int faltasCometidas;
    private int faltasRecibidas;
    private int penaltisRecibidos;
    private int penaltisCometidos;

    private int pasesAcertados;
    private int pasesFallados;
    private int robosDeBalon;
    private int tirosCompletados;
    private int tirosEntreLosTresPalos;
    private int tiempoJugado;
    private int tarjetasAmarillas;
    private int tarjetasRojas;

    private double totalFieldPoints;
    private double totalGoalKeeperPoints;
}