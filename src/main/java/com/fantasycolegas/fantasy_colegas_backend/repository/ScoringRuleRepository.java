package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link ScoringRule}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de las reglas de puntuación.
 * </p>
 */
@Repository
public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
    /**
     * Busca todas las reglas de puntuación que se aplican a un rol de jugador específico.
     *
     * @param role El rol del jugador (ej. PORTERO o CAMPO).
     * @return Una lista de reglas de puntuación para el rol dado.
     */
    List<ScoringRule> findAllByRole(PlayerTeamRole role);
}