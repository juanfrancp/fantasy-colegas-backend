package com.fantasycolegas.fantasy_colegas_backend.util;

import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.repository.PlayerRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final PlayerRepository playerRepository;

    public DataInitializer(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializePlaceholderPlayer() {
        // Buscamos si ya existe un jugador vacío
        if (playerRepository.findByIsPlaceholderTrue().isEmpty()) {
            // Si no existe, lo creamos
            Player placeholder = new Player();
            placeholder.setName("Jugador Vacío");
            placeholder.setImage("https://example.com/placeholder-image.png");
            placeholder.setTotalPoints(0);
            placeholder.setPlaceholder(true);
            // El league_id se queda como null por defecto en este caso
            playerRepository.save(placeholder);
            System.out.println("Jugador vacío creado correctamente.");
        }
    }
}