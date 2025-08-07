package com.fantasycolegas.fantasy_colegas_backend.util;

import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.ScoringRuleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final PlayerRepository playerRepository;
    private final ScoringRuleRepository scoringRuleRepository;

    public DataInitializer(PlayerRepository playerRepository, ScoringRuleRepository scoringRuleRepository) {
        this.playerRepository = playerRepository;
        this.scoringRuleRepository = scoringRuleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializePlaceholderPlayer() {
        if (playerRepository.findByIsPlaceholderTrue().isEmpty()) {
            Player placeholder = new Player();
            placeholder.setName("Jugador Vacío");
            placeholder.setImage("https://example.com/placeholder-image.png");
            placeholder.setTotalPoints(0);
            placeholder.setPlaceholder(true);
            playerRepository.save(placeholder);
            System.out.println("Jugador vacío creado correctamente.");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeScoringRules() {
        if (scoringRuleRepository.count() == 0) {
            // Reglas para jugadores de CAMPO
            scoringRuleRepository.save(createRule("golesMarcados", 5.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("asistencias", 3.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("fallosClarosDeGol", -1.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("faltasCometidas", -0.5, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("faltasRecibidas", 0.5, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("tarjetasAmarillas", -1.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("tarjetasRojas", -3.0, PlayerTeamRole.CAMPO));

            // Reglas para jugadores de PORTERO
            scoringRuleRepository.save(createRule("paradasComoPortero", 0.5, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("golesEncajadosComoPortero", -2.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("penaltisRecibidos", 2.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("penaltisCometidos", -3.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("tarjetasAmarillas", -1.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("tarjetasRojas", -3.0, PlayerTeamRole.PORTERO));

            System.out.println("Reglas de puntuación iniciales creadas correctamente.");
        }
    }

    private ScoringRule createRule(String statName, double pointsPerUnit, PlayerTeamRole playerTeamRole) {
        ScoringRule rule = new ScoringRule();
        rule.setStatName(statName);
        rule.setPointsPerUnit(pointsPerUnit);
        rule.setRole(playerTeamRole);
        return rule;
    }
}