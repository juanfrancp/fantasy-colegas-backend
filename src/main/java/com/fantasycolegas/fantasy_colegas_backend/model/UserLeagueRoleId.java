package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Clase para la clave primaria compuesta de la entidad {@link UserLeagueRole}.
 * <p>
 * Esta clave se utiliza para identificar de forma única la relación entre un usuario y una liga.
 * Es una clase embebible que contiene los IDs del usuario y de la liga.
 * </p>
 */
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