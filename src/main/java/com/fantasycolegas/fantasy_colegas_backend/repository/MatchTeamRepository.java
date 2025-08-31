package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {
}