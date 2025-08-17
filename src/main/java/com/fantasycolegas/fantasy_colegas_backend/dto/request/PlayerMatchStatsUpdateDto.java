package com.fantasycolegas.fantasy_colegas_backend.dto.request;

import jakarta.validation.constraints.Min;
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

    @Min(value = 0, message = "Los goles marcados no pueden ser negativos")
    private int golesMarcados = 0;

    @Min(value = 0, message = "Los fallos claros de gol no pueden ser negativos")
    private int fallosClarosDeGol = 0;

    @Min(value = 0, message = "Las asistencias no pueden ser negativas")
    private int asistencias = 0;

    @Min(value = 0, message = "Los goles encajados como portero no pueden ser negativos")
    private int golesEncajadosComoPortero = 0;

    @Min(value = 0, message = "Las paradas como portero no pueden ser negativas")
    private int paradasComoPortero = 0;

    @Min(value = 0, message = "Las cesiones concedidas no pueden ser negativas")
    private int cesionesConcedidas = 0;

    @Min(value = 0, message = "Las faltas cometidas no pueden ser negativas")
    private int faltasCometidas = 0;

    @Min(value = 0, message = "Las faltas recibidas no pueden ser negativas")
    private int faltasRecibidas = 0;

    @Min(value = 0, message = "Los penaltis recibidos no pueden ser negativos")
    private int penaltisRecibidos = 0;

    @Min(value = 0, message = "Los penaltis cometidos no pueden ser negativos")
    private int penaltisCometidos = 0;

    @Min(value = 0, message = "Los pases acertados no pueden ser negativos")
    private int pasesAcertados = 0;

    @Min(value = 0, message = "Los pases fallados no pueden ser negativos")
    private int pasesFallados = 0;

    @Min(value = 0, message = "Los robos de balón no pueden ser negativos")
    private int robosDeBalon = 0;

    @Min(value = 0, message = "Los tiros completados no pueden ser negativos")
    private int tirosCompletados = 0;

    @Min(value = 0, message = "Los tiros entre los tres palos no pueden ser negativos")
    private int tirosEntreLosTresPalos = 0;

    @Min(value = 0, message = "El tiempo jugado no puede ser negativo")
    private int tiempoJugado = 0;

    @Min(value = 0, message = "Las tarjetas amarillas no pueden ser negativas")
    private int tarjetasAmarillas = 0;

    @Min(value = 0, message = "Las tarjetas rojas no pueden ser negativas")
    private int tarjetasRojas = 0;
}