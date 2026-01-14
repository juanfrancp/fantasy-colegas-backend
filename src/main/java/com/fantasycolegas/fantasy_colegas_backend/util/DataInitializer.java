package com.fantasycolegas.fantasy_colegas_backend.util;

import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.model.ScoringRule;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import com.fantasycolegas.fantasy_colegas_backend.repository.ScoringRuleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Clase de inicialización de datos para la aplicación.
 * <p>
 * Se ejecuta al arrancar la aplicación y se encarga de asegurar que existan
 * ciertos datos iniciales, como un jugador "placeholder" y las reglas de puntuación
 * por defecto, si aún no se han creado.
 * </p>
 */
@Component
public class DataInitializer {

    private final PlayerRepository playerRepository;
    private final ScoringRuleRepository scoringRuleRepository;

    /**
     * Constructor que inyecta las dependencias de los repositorios.
     *
     * @param playerRepository      Repositorio de jugadores.
     * @param scoringRuleRepository Repositorio de reglas de puntuación.
     */
    public DataInitializer(PlayerRepository playerRepository, ScoringRuleRepository scoringRuleRepository) {
        this.playerRepository = playerRepository;
        this.scoringRuleRepository = scoringRuleRepository;
    }

    /**
     * Inicializa un jugador "placeholder" al arrancar la aplicación, si no existe.
     * <p>
     * Este jugador se utiliza para llenar posiciones vacías en los rosters de los equipos.
     * </p>
     */
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

    /**
     * Inicializa las reglas de puntuación por defecto al arrancar la aplicación.
     * <p>
     * Crea un conjunto de reglas de puntuación para los roles de jugador de campo
     * y portero, si la base de datos no contiene ninguna regla.
     * </p>
     */

    @EventListener(ApplicationReadyEvent.class)
    public void initializeScoringRules() {
        if (scoringRuleRepository.count() == 0) {
            // --- JUGADOR DE CAMPO ---
            scoringRuleRepository.save(createRule("golesMarcados", 5.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("asistencias", 3.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("tirosEntreLosTresPalos", 1.0, PlayerTeamRole.CAMPO)); // Bonus ofensivo
            scoringRuleRepository.save(createRule("fallosClarosDeGol", -2.0, PlayerTeamRole.CAMPO)); // Penalización
            scoringRuleRepository.save(createRule("salvadasDeGol", 3.0, PlayerTeamRole.CAMPO)); // Sacarla bajo palos
            scoringRuleRepository.save(createRule("penaltisCometidos", -3.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("penaltisRecibidos", 2.0, PlayerTeamRole.CAMPO));
            scoringRuleRepository.save(createRule("tarjetasAmarillas", -2.0, PlayerTeamRole.CAMPO)); // Subido a -2
            scoringRuleRepository.save(createRule("tarjetasRojas", -5.0, PlayerTeamRole.CAMPO)); // Subido a -5

            // --- PORTERO ---
            scoringRuleRepository.save(createRule("golesMarcados", 10.0, PlayerTeamRole.PORTERO)); // ¡Gol de portero vale más!
            scoringRuleRepository.save(createRule("asistencias", 5.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("paradasComoPortero", 1.0, PlayerTeamRole.PORTERO)); // 1 pto por parada
            scoringRuleRepository.save(createRule("penaltisParados", 5.0, PlayerTeamRole.PORTERO)); // Momento héroe
            scoringRuleRepository.save(createRule("porteriaImbatida", 3.0, PlayerTeamRole.PORTERO)); // Clean Sheet
            scoringRuleRepository.save(createRule("golesEncajadosComoPortero", -1.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("tarjetasAmarillas", -2.0, PlayerTeamRole.PORTERO));
            scoringRuleRepository.save(createRule("tarjetasRojas", -5.0, PlayerTeamRole.PORTERO));

            System.out.println("Reglas de puntuación inicializadas correctamente.");
        }
    }

    /**
     * Método auxiliar para crear una regla de puntuación.
     *
     * @param statName       El nombre de la estadística.
     * @param pointsPerUnit  Los puntos por unidad de la estadística.
     * @param playerTeamRole El rol al que se aplica la regla.
     * @return Una nueva instancia de {@link ScoringRule}.
     */
    private ScoringRule createRule(String statName, double pointsPerUnit, PlayerTeamRole playerTeamRole) {
        ScoringRule rule = new ScoringRule();
        rule.setStatName(statName);
        rule.setPointsPerUnit(pointsPerUnit);
        rule.setRole(playerTeamRole);
        return rule;
    }
}