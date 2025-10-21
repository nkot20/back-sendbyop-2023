package com.sendByOP.expedition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;

/**
 * Configuration de Thymeleaf pour le rendu des templates d'emails
 */
@Configuration
public class ThymeleafConfig {

    /**
     * Configure le résolveur de templates pour les emails
     * Les templates seront recherchés dans src/main/resources/templates/
     */
    @Bean
    public SpringResourceTemplateResolver emailTemplateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false); // Désactiver le cache en développement
        templateResolver.setOrder(1);
        return templateResolver;
    }

    /**
     * Configure le moteur de template Thymeleaf
     */
    @Bean
    public SpringTemplateEngine thymeleafTemplateEngine(SpringResourceTemplateResolver emailTemplateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(emailTemplateResolver);
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
}
