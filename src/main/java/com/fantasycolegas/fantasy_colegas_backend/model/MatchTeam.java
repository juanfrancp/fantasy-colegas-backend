package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "match_teams")
public class MatchTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Relación: Un equipo del partido pertenece a un solo partido.
    // Usamos OneToOne porque un partido tiene un equipo local y otro visitante.
    // Esta es la inversa de la relación en Match.
    @OneToOne(mappedBy = "homeTeam")
    private Match matchAsHomeTeam;

    @OneToOne(mappedBy = "awayTeam")
    private Match matchAsAwayTeam;

    // Relación: Un equipo del partido está compuesto por muchos jugadores.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "match_team_players",
            joinColumns = @JoinColumn(name = "match_team_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> players = new ArrayList<>();

    public MatchTeam(String name, List<Player> players) {
        this.name = name;
        this.players = players;
    }
}