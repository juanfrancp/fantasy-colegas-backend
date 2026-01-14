package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_match_lineups")
@Data
@NoArgsConstructor
public class UserMatchLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerTeamRole role; // Guardamos el rol que tenía (PORTERO o CAMPO) en ese momento

    // Constructor helper
    public UserMatchLineup(User user, Match match, Player player, PlayerTeamRole role) {
        this.user = user;
        this.match = match;
        this.player = player;
        this.role = role;
    }
}