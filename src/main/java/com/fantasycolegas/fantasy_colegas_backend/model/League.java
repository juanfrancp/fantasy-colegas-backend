package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

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

    private String joinCode; // Código de 4 dígitos para unirse a la liga

    private int numberOfPlayers; // 5v5, 6v6, etc.

    // Relación con User: Un usuario es el administrador de la liga [cite: 29]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // Relación con User: Muchos usuarios pueden participar en una liga
    @ManyToMany
    @JoinTable(
            name = "league_participants",
            joinColumns = @JoinColumn(name = "league_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;

    // Relación con Player: Una liga tiene muchos jugadores
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Player> players;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        League league = (League) o;
        // Solo compara por el ID
        return id != null && Objects.equals(id, league.id);
    }

    @Override
    public int hashCode() {
        // Solo usa el ID para el hash
        return getClass().hashCode();
    }
}