package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime; // Importamos LocalDateTime

/**
 * @author Juan Francisco Carceles
 * @version 1.1
 * @since 31/08/2025
 * <p>
 * Entidad JPA que representa un partido entre dos equipos de partido (MatchTeam).
 * </p>
 */
@Entity
@Data
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    // Relación con el equipo local
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "home_team_id", referencedColumnName = "id")
    private MatchTeam homeTeam;

    // Relación con el equipo visitante
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "away_team_id", referencedColumnName = "id")
    private MatchTeam awayTeam;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(nullable = false)
    private LocalDateTime matchDate; // Cambiado a LocalDateTime
}