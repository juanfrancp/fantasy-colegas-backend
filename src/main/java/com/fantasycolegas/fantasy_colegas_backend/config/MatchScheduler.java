package com.fantasycolegas.fantasy_colegas_backend.config;

import com.fantasycolegas.fantasy_colegas_backend.model.Match;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.MatchStatus;
import com.fantasycolegas.fantasy_colegas_backend.repository.MatchRepository;
import com.fantasycolegas.fantasy_colegas_backend.service.MatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MatchScheduler {

    private final MatchRepository matchRepository;
    private final MatchService matchService;

    public MatchScheduler(MatchRepository matchRepository, MatchService matchService) {
        this.matchRepository = matchRepository;
        this.matchService = matchService;
    }

    // Se ejecuta cada 60000 ms = 1 minuto
    @Scheduled(fixedRate = 60000)
    public void checkAndLockMatches() {
        // 1. Buscamos partidos que estén 'SCHEDULED' y cuya hora sea ANTERIOR a 'ahora'
        List<Match> matchesToStart = matchRepository.findByStatusAndMatchDateBefore(
                MatchStatus.SCHEDULED,
                LocalDateTime.now()
        );

        // 2. Procesamos cada partido encontrado
        for (Match match : matchesToStart) {
            try {
                System.out.println("Iniciando partido automáticamente: " + match.getId());
                // Llamamos a tu método existente que hace la "foto" y bloquea el partido
                matchService.lockMatchAndSnapshot(match.getId());
            } catch (Exception e) {
                System.err.println("Error al iniciar el partido " + match.getId() + ": " + e.getMessage());
            }
        }
    }
}