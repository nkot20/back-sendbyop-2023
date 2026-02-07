package com.sendByOP.expedition.services.impl;

import jakarta.annotation.PostConstruct;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service d'envoi d'emails via l'API HTTP Mailgun
 * Alternative à SendGrid, fonctionne aussi sur Render
 */
@Service
@Slf4j
public class MailgunApiService {
    
    @Value("${mailgun.api.key}")
    private String mailgunApiKey;
    
    @Value("${mailgun.domain:sendbyop.net}")
    private String mailgunDomain;
    
    @Value("${email.from}")
    private String emailFrom;
    
    @PostConstruct
    public void init() {
        log.info("MailgunApiService initialisé");
        log.info("Email FROM: {}", emailFrom);
        log.info("Mailgun Domain: {}", mailgunDomain);
        log.info("API Key configurée: {}",
                mailgunApiKey != null && !mailgunApiKey.isEmpty() && !mailgunApiKey.contains("${") 
                ? "OUI (longueur: " + mailgunApiKey.length() + " caractères)" 
                : "NON ou INVALIDE");
        if (mailgunApiKey == null || mailgunApiKey.isEmpty() || mailgunApiKey.contains("${")) {
            log.error("MAILGUN API KEY NON CONFIGURÉE ! Vérifiez la variable d'environnement API_EMAIL_KEY");
        }
    }
    
    /**
     * Envoie un email HTML via l'API Mailgun
     * 
     * @param toEmail Email du destinataire
     * @param subject Sujet de l'email
     * @param htmlContent Contenu HTML
     * @throws IOException En cas d'erreur API
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws IOException {
        log.info("[Mailgun API] Envoi d'email à: {}", toEmail);
        log.info("[Mailgun API] FROM: {}", emailFrom);
        log.info("[Mailgun API] Domain: {}", mailgunDomain);
        
        try {
            String apiUrl = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";
            log.info("🔧 [Mailgun API] URL: {}", apiUrl);
            
            HttpResponse<JsonNode> response = Unirest.post(apiUrl)
                    .basicAuth("api", mailgunApiKey)
                    .queryString("from", "SendByOp <" + emailFrom + ">")
                    .queryString("to", toEmail)
                    .queryString("subject", subject)
                    .queryString("html", htmlContent)
                    .asJson();
            
            log.info("[Mailgun API] Réponse reçue - Status: {}", response.getStatus());
            log.info("[Mailgun API] Body: {}", response.getBody());
            
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                log.info("[Mailgun API] Email envoyé avec succès à: {} (Status: {})",
                        toEmail, response.getStatus());
            } else {
                log.error("[Mailgun API] Erreur lors de l'envoi (Status: {}): {}",
                        response.getStatus(), response.getBody());
                throw new IOException("Mailgun API error: " + response.getBody());
            }
            
        } catch (UnirestException e) {
            log.error("[Mailgun API] Exception UnirestException: {}", e.getMessage(), e);
            throw new IOException("Erreur Mailgun: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[Mailgun API] Exception inattendue: {}", e.getMessage(), e);
            throw new IOException("Erreur inattendue: " + e.getMessage(), e);
        }
    }
    
    /**
     * Envoie un email texte simple
     */
    public void sendTextEmail(String toEmail, String subject, String textContent) throws IOException {
        log.info("[Mailgun API] Envoi d'email texte à: {}", toEmail);
        
        try {
            String apiUrl = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";
            
            HttpResponse<JsonNode> response = Unirest.post(apiUrl)
                    .basicAuth("api", mailgunApiKey)
                    .queryString("from", "SendByOp <" + emailFrom + ">")
                    .queryString("to", toEmail)
                    .queryString("subject", subject)
                    .queryString("text", textContent)
                    .asJson();
            
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                log.info("[Mailgun API] Email texte envoyé avec succès à: {}", toEmail);
            } else {
                log.error("[Mailgun API] Erreur (Status: {}): {}",
                        response.getStatus(), response.getBody());
                throw new IOException("Mailgun API error: " + response.getBody());
            }
            
        } catch (UnirestException e) {
            log.error("[Mailgun API] Exception: {}", e.getMessage(), e);
            throw new IOException("Erreur Mailgun: " + e.getMessage(), e);
        }
    }
}
