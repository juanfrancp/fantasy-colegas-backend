package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.LeagueJoinRequest;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueJoinRequestRepository extends JpaRepository<LeagueJoinRequest, Long> {
    List<LeagueJoinRequest> findByLeagueAndStatus(League league, RequestStatus status);

    Optional<LeagueJoinRequest> findByUserAndLeagueAndStatus(User user, League league, RequestStatus status);

    Optional<LeagueJoinRequest> findById(Long id);
}