package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "leagues")
@Data
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
}