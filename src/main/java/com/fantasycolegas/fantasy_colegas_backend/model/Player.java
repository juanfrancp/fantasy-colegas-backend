package com.fantasycolegas.fantasy_colegas_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Entidad JPA que representa a un jugador en el contexto de una liga.
 * <p>
 * Cada jugador tiene un nombre, una imagen, puntos totales, y está asociado
 * a una liga. Puede ser un jugador real o un 'placeholder' para un puesto libre en un equipo.
 * </p>
 */
@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'https://example.com/default-player.jpg'")
    private String image;

    @Column(nullable = false)
    private int totalPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @Column(nullable = false)
    private boolean isPlaceholder = false;
}