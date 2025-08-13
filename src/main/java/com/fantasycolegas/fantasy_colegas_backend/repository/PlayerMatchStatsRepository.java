package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.PlayerMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link PlayerMatchStats}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de las estadísticas de los jugadores en los partidos.
 * </p>
 */
@Repository
public interface PlayerMatchStatsRepository extends JpaRepository<PlayerMatchStats, Long> {

    /**
     * Busca las estadísticas de un jugador para un partido específico.
     *
     * @param matchId El ID del partido.
     * @param id      El ID del jugador.
     * @return Un objeto {@link Optional} que puede contener las estadísticas encontradas.
     */
    Optional<PlayerMatchStats> findByMatchIdAndPlayerId(Long matchId, Long id);

    /**
     * Busca todas las estadísticas de un jugador en todos los partidos.
     *
     * @param id El ID del jugador.
     * @return Una lista de estadísticas de partidos del jugador.
     */
    List<PlayerMatchStats> findByPlayerId(Long id);
}