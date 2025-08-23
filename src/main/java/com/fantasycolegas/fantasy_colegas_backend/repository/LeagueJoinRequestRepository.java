package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT r FROM LeagueJoinRequest r JOIN FETCH r.user WHERE r.league = :league AND r.status = :status")
    List<LeagueJoinRequest> findByLeagueAndStatusWithUser(
            @Param("league") League league,
            @Param("status") RequestStatus status
    );

    Optional<LeagueJoinRequest> findByUserAndLeagueAndStatus(User user, League league, RequestStatus status);

    Optional<LeagueJoinRequest> findById(Long id);

    Optional<LeagueJoinRequest> findByUserIdAndLeagueIdAndStatus(Long userId, Long leagueId, RequestStatus status);

    List<LeagueJoinRequest> findByUserIdAndStatus(Long userId, RequestStatus status);

    void deleteAllByLeague(League league);
}