package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.Match;
import com.fantasycolegas.fantasy_colegas_backend.model.UserMatchLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMatchLineupRepository extends JpaRepository<UserMatchLineup, Long> {
    List<UserMatchLineup> findByMatch(Match match);

    List<UserMatchLineup> findByUserIdAndMatchLeagueId(Long userId, Long leagueId);
}
