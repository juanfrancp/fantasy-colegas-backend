package com.fantasycolegas.fantasy_colegas_backend.startup;

import com.fantasycolegas.fantasy_colegas_backend.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentStartupRunner implements CommandLineRunner {

    private final FileStorageService fileStorageService;

    public DevelopmentStartupRunner(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- DEVELOPMENT MODE: Cleaning up upload directory... ---");
        fileStorageService.deleteAll();
        fileStorageService.init();
        System.out.println("--- Upload directory cleaned and initialized. ---");
    }
}