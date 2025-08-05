// Archivo: LeagueJoinRequestRepository.java
package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.model.RequestStatus;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueJoinRequestRepository extends JpaRepository<LeagueJoinRequest, Long> {
    // Buscar todas las solicitudes pendientes de una liga
    List<LeagueJoinRequest> findByLeagueAndStatus(League league, RequestStatus status);

    // Buscar si ya existe una solicitud pendiente de un usuario para una liga
    Optional<LeagueJoinRequest> findByUserAndLeagueAndStatus(User user, League league, RequestStatus status);

    // Encontrar una solicitud específica por su ID
    Optional<LeagueJoinRequest> findById(Long id);
}