package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 *
 * Entidad JPA que define una regla de puntuación para una estadística de jugador.
 * <p>
 * Cada regla de puntuación asocia un nombre de estadística (por ejemplo, "golesMarcados")
 * con una cantidad de puntos y se aplica a un rol de jugador específico (por ejemplo, "DELANTERO").
 * </p>
 */
@Entity
@Data
@Table(name = "scoring_rules")
public class ScoringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String statName;

    @Column(nullable = false)
    private double pointsPerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerTeamRole role;

}