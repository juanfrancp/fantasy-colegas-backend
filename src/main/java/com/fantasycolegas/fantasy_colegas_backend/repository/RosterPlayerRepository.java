package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.RosterPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RosterPlayerRepository extends JpaRepository<RosterPlayer, Long> {
    void deleteByUserIdAndLeagueId(Long userId, Long leagueId);
    List<RosterPlayer> findByUserIdAndLeagueId(Long userId, Long leagueId);
    List<RosterPlayer> findByLeagueAndUser_Id(League league, Long userId);
}