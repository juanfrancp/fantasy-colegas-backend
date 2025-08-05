package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLeagueRoleId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "league_id")
    private Long leagueId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLeagueRoleId that = (UserLeagueRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(leagueId, that.leagueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, leagueId);
    }
}