package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_match_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private double totalGoalkeeperPoints;

}