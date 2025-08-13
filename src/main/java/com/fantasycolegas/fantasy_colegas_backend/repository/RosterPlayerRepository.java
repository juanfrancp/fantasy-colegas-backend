package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.RosterPlayer;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link RosterPlayer}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de los rosters de los usuarios en las ligas.
 * </p>
 */
@Repository
public interface RosterPlayerRepository extends JpaRepository<RosterPlayer, Long> {

    /**
     * Elimina todos los jugadores del roster de un usuario en una liga específica.
     *
     * @param userId   El ID del usuario.
     * @param leagueId El ID de la liga.
     */
    void deleteByUserIdAndLeagueId(Long userId, Long leagueId);

    /**
     * Busca todos los jugadores del roster de un usuario en una liga específica.
     *
     * @param userId   El ID del usuario.
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link RosterPlayer} que forman el roster.
     */
    List<RosterPlayer> findByUserIdAndLeagueId(Long userId, Long leagueId);

    /**
     * Busca todos los jugadores del roster de un usuario en una liga.
     *
     * @param league La liga.
     * @param userId El ID del usuario.
     * @return Una lista de {@link RosterPlayer}.
     */
    List<RosterPlayer> findByLeagueAndUser_Id(League league, Long userId);

    /**
     * Busca un jugador específico en el roster de un usuario, en una liga y con un rol dados.
     *
     * @param userId         El ID del usuario.
     * @param leagueId       El ID de la liga.
     * @param playerTeamRole El rol del jugador en el equipo.
     * @param id             El ID del jugador.
     * @return Un objeto {@link Optional} que puede contener el {@link RosterPlayer} encontrado.
     */
    Optional<RosterPlayer> findByUserIdAndLeagueIdAndRoleAndPlayerId(Long userId, Long leagueId, PlayerTeamRole playerTeamRole, Long id);

    /**
     * Busca la primera instancia de un jugador específico en el roster de un usuario, en una liga y con un rol dados.
     * Este método es similar a `findByUserIdAndLeagueIdAndRoleAndPlayerId` pero más específico en la primera coincidencia.
     *
     * @param userId         El ID del usuario.
     * @param leagueId       El ID de la liga.
     * @param playerTeamRole El rol del jugador en el equipo.
     * @param id             El ID del jugador.
     * @return Un objeto {@link Optional} que puede contener el {@link RosterPlayer} encontrado.
     */
    Optional<RosterPlayer> findFirstByUserIdAndLeagueIdAndRoleAndPlayerId(Long userId, Long leagueId, PlayerTeamRole playerTeamRole, Long id);

    /**
     * Verifica si un jugador existe en el roster de un usuario dentro de una liga.
     *
     * @param userId   El ID del usuario.
     * @param leagueId El ID de la liga.
     * @param playerId El ID del jugador.
     * @return {@code true} si el jugador existe en el roster, {@code false} en caso contrario.
     */
    boolean existsByUserIdAndLeagueIdAndPlayerId(Long userId, Long leagueId, Long playerId);

    /**
     * Busca todos los rosters que contienen a un jugador específico.
     *
     * @param playerId El ID del jugador.
     * @return Una lista de {@link RosterPlayer} que contienen al jugador.
     */
    List<RosterPlayer> findAllByPlayerId(Long playerId);

    /**
     * Busca todos los jugadores de todos los rosters en una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link RosterPlayer}.
     */
    List<RosterPlayer> findAllByLeagueId(Long leagueId);

    /**
     * Obtiene una lista de los IDs de usuarios únicos que tienen un roster en una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de los IDs de los usuarios.
     */
    @Query("SELECT DISTINCT rp.user.id FROM RosterPlayer rp WHERE rp.league.id = :leagueId")
    List<Long> findDistinctUserIdsByLeagueId(Long leagueId);
}