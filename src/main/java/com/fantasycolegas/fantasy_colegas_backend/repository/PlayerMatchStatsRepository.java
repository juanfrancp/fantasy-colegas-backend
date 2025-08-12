package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.PlayerMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerMatchStatsRepository extends JpaRepository<PlayerMatchStats, Long> {
    Optional<PlayerMatchStats> findByMatchIdAndPlayerId(Long matchId, Long id);

    List<PlayerMatchStats> findByPlayerId(Long id);
}