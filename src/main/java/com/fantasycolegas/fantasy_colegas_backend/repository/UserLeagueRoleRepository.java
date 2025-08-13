package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.UserLeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.model.UserLeagueRoleId;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link UserLeagueRole}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de los roles de los usuarios en las ligas.
 * </p>
 */
@Repository
public interface UserLeagueRoleRepository extends JpaRepository<UserLeagueRole, UserLeagueRoleId> {

    /**
     * Busca todos los roles de los usuarios que pertenecen a una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @return Una lista de {@link UserLeagueRole}.
     */
    List<UserLeagueRole> findAllByLeagueId(Long leagueId);

    /**
     * Verifica si un usuario con un ID dado existe en una liga específica.
     *
     * @param leagueId El ID de la liga.
     * @param userId   El ID del usuario.
     * @return {@code true} si el usuario existe en la liga, {@code false} en caso contrario.
     */
    boolean existsByLeagueIdAndUserId(Long leagueId, Long userId);

    /**
     * Busca el rol de un usuario específico en una liga dada.
     *
     * @param leagueId El ID de la liga.
     * @param userId   El ID del usuario.
     * @return Un objeto {@link Optional} que puede contener el {@link UserLeagueRole} encontrado.
     */
    Optional<UserLeagueRole> findByLeagueIdAndUserId(Long leagueId, Long userId);

    /**
     * Cuenta el número de usuarios con un rol específico en una liga.
     *
     * @param leagueId El ID de la liga.
     * @param role     El rol a contar (ej. ADMIN).
     * @return El número de usuarios con ese rol en la liga.
     */
    long countByLeagueIdAndRole(Long leagueId, LeagueRole role);
}