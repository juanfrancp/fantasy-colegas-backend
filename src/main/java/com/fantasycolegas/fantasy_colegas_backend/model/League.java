package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 *
 * Entidad JPA que representa una liga en la aplicación.
 * <p>
 * Una liga agrupa a varios usuarios (participantes) y jugadores, y define
 * las reglas básicas del juego, como si es privada o no, el código de unión,
 * el número de jugadores y el tamaño del equipo.
 * </p>
 */

@Entity
@Table(name = "leagues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String description;
    private String image;
    private boolean isPrivate = false;
    private String joinCode;
    private int numberOfPlayers;
    private int teamSize;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserLeagueRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Player> players = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        League league = (League) o;
        return id != null && Objects.equals(id, league.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}