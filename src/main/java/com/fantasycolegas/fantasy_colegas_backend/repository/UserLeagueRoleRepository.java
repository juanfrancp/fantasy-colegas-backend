package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.UserLeagueRole;
import com.fantasycolegas.fantasy_colegas_backend.model.UserLeagueRoleId;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.LeagueRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLeagueRoleRepository extends JpaRepository<UserLeagueRole, UserLeagueRoleId> {
    List<UserLeagueRole> findAllByLeagueId(Long leagueId);

    boolean existsByLeagueIdAndUserId(Long leagueId, Long userId);

    Optional<UserLeagueRole> findByLeagueIdAndUserId(Long leagueId, Long userId);

    long countByLeagueIdAndRole(Long leagueId, LeagueRole role);
}