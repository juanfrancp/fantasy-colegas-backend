package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "user_league_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLeagueRole implements Serializable {

    @EmbeddedId
    private UserLeagueRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("leagueId")
    @JoinColumn(name = "league_id")
    private League league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeagueRole role;

    public UserLeagueRole(User user, League league, LeagueRole role) {
        this.user = user;
        this.league = league;
        this.role = role;
        this.id = new UserLeagueRoleId(user.getId(), league.getId());
    }
}