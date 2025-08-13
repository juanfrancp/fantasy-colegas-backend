package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Entidad JPA que representa la relación entre un usuario, una liga y un jugador,
 * lo que forma un roster o equipo.
 * <p>
 * Esta clase asocia un {@link User} y un {@link Player} dentro de una {@link League},
 * asignándole un rol o posición específica dentro del equipo.
 * </p>
 */
@Entity
@Table(name = "roster_players")
@Data
public class RosterPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerTeamRole role;
}