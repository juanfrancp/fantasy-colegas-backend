package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "player_match_stats")
public class PlayerMatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // Estadísticas obligatorias
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

    // Estadísticas opcionales
    private int pasesAcertados = 0;
    private int pasesFallados = 0;
    private int robosDeBalon = 0;
    private int tirosCompletados = 0;
    private int tirosEntreLosTresPalos = 0;
    private int tiempoJugado = 0;
    private int tarjetasAmarillas = 0;
    private int tarjetasRojas = 0;

    // Puntuación calculada
    private double totalPoints = 0.0;
}