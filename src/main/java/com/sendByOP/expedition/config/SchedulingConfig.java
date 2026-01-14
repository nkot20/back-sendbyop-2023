package com.sendByOP.expedition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration pour activer le scheduling des tâches automatisées
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // La simple présence de @EnableScheduling active le scheduling
    // Les méthodes annotées avec @Scheduled seront exécutées automatiquement
}
