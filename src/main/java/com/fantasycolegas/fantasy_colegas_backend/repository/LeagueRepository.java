package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link League}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de las ligas, así como consultas personalizadas.
 * </p>
 */
@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    /**
     * Busca una liga por su código de unión.
     *
     * @param joinCode El código de unión de la liga.
     * @return Un objeto {@link Optional} que puede contener la liga encontrada.
     */
    Optional<League> findByJoinCode(String joinCode);

}