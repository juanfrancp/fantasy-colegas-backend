package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Repositorio para la entidad {@link LeagueJoinRequest}.
 * <p>
 * Proporciona métodos para interactuar con la base de datos para la gestión
 * de las solicitudes de unión a ligas.
 * </p>
 */
@Repository
public interface LeagueJoinRequestRepository extends JpaRepository<LeagueJoinRequest, Long> {

    /**
     * Busca todas las solicitudes de unión para una liga específica con un estado dado.
     *
     * @param league La liga a buscar.
     * @param status El estado de la solicitud (ej. PENDING).
     * @return Una lista de solicitudes de unión.
     */
    List<LeagueJoinRequest> findByLeagueAndStatus(League league, RequestStatus status);

    /**
     * Busca una solicitud de unión de un usuario específico a una liga con un estado dado.
     *
     * @param user   El usuario que hizo la solicitud.
     * @param league La liga a la que se solicitó unirse.
     * @param status El estado de la solicitud.
     * @return Un objeto {@link Optional} que puede contener la solicitud encontrada.
     */
    Optional<LeagueJoinRequest> findByUserAndLeagueAndStatus(User user, League league, RequestStatus status);

    /**
     * Busca una solicitud de unión por su ID.
     *
     * @param id El ID de la solicitud.
     * @return Un objeto {@link Optional} que puede contener la solicitud encontrada.
     */
    Optional<LeagueJoinRequest> findById(Long id);
}