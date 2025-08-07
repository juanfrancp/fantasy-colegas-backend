// Archivo: ScoringRule.java
package com.fantasycolegas.fantasy_colegas_backend.model;

import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import jakarta.persistence.*;
import lombok.Data;

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