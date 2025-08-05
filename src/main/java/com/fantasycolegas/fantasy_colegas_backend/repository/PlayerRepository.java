package com.fantasycolegas.fantasy_colegas_backend.repository;

import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByLeagueId(Long leagueId);

    // Método para encontrar el jugador vacío
    Optional<Player> findByIsPlaceholderTrue();

    // Método para encontrar jugadores que no son placeholder en una liga
    List<Player> findByLeagueIdAndIsPlaceholderFalse(Long leagueId);
}