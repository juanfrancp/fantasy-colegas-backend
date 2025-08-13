package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * DTO (Data Transfer Object) para la actualización de las estadísticas de un jugador en un partido.
 * <p>
 * Contiene todas las métricas que se pueden registrar para un jugador durante un partido
 * de fantasía. Todos los campos de estadísticas tienen un valor por defecto de 0.
 * </p>
 */
@Data
public class PlayerMatchStatsUpdateDto {

    @NotNull(message = "El ID del jugador es obligatorio.")
    private Long playerId;

    private int golesMarcados = 0;
    private int fallosClarosDeGol = 0;
    private int asistencias = 0;
    private int golesEncajadosComoPortero = 0;
    private int paradasComoPortero = 0;
    private int cesionesConcedidas = 0;
    private int faltasCometidas = 0;
    private int faltasRecibidas = 0;
    private int penaltisRecibidos = 0;
    private int penaltisCometidos = 0;
    
    private int pasesAcertados = 0;
    private int pasesFallados = 0;
    private int robosDeBalon = 0;
    private int tirosCompletados = 0;
    private int tirosEntreLosTresPalos = 0;
    private int tiempoJugado = 0;
    private int tarjetasAmarillas = 0;
    private int tarjetasRojas = 0;
}