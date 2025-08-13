package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Entidad JPA que representa la relación entre un usuario y una liga,
 * incluyendo el rol que el usuario tiene dentro de dicha liga.
 * <p>
 * Esta clase utiliza una clave compuesta ({@link UserLeagueRoleId}) para
 * identificar de forma única la combinación de un usuario y una liga.
 * </p>
 */
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

    /**
     * Constructor para crear una nueva instancia de {@link UserLeagueRole}.
     * <p>
     * Inicializa la relación con un usuario, una liga y un rol específico,
     * y crea la clave compuesta automáticamente.
     * </p>
     *
     * @param user   El usuario que participa en la liga.
     * @param league La liga a la que pertenece el usuario.
     * @param role   El rol del usuario en la liga (ej. ADMIN, PARTICIPANT).
     */
    public UserLeagueRole(User user, League league, LeagueRole role) {
        this.user = user;
        this.league = league;
        this.role = role;
        this.id = new UserLeagueRoleId(user.getId(), league.getId());
    }
}