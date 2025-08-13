package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link Player}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de los jugadores.
 * </p>
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    /**
     * Busca todos los jugadores que pertenecen a una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de jugadores de la liga.
     */
    List<Player> findAllByLeagueId(Long leagueId);

    /**
     * Busca un jugador 'placeholder'.
     *
     * @return Un objeto {@link Optional} que puede contener el jugador 'placeholder' encontrado.
     */
    Optional<Player> findByIsPlaceholderTrue();

    /**
     * Busca todos los jugadores que no son 'placeholder' en una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de jugadores reales en la liga.
     */
    List<Player> findByLeagueIdAndIsPlaceholderFalse(Long leagueId);

}