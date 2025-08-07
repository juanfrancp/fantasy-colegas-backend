package com.fantasycolegas.fantasy_colegas_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMatchStatsResponseDto {
    private Long id;
    private Long playerId;

    // Estadísticas obligatorias
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

    // Estadísticas opcionales
    private int pasesAcertados;
    private int pasesFallados;
    private int robosDeBalon;
    private int tirosCompletados;
    private int tirosEntreLosTresPalos;
    private int tiempoJugado;
    private int tarjetasAmarillas;
    private int tarjetasRojas;

    // Puntos totales calculados
    private double totalPoints;
}