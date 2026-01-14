package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.Match;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link Match}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de los partidos.
 * </p>
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Busca todos los partidos que pertenecen a una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de partidos de la liga.
     */
    List<Match> findAllByLeagueId(Long leagueId);

    /**
     * Cuenta el número de partidos en una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return El número de partidos.
     */
    long countByLeagueId(Long leagueId);

    void deleteAllByLeague(League league);

    List<Match> findAllByLeague(League league);

    List<Match> findByMatchDateAfter(LocalDateTime date);

    List<Match> findByMatchDateBefore(LocalDateTime date);

    List<Match> findByStatusAndMatchDateBefore(MatchStatus status, LocalDateTime date);
}