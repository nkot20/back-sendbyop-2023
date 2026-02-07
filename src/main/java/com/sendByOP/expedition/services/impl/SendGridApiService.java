package com.sendByOP.expedition.services.impl;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service d'envoi d'emails via l'API HTTP SendGrid
 * Utilisé en production sur Render (évite les blocages SMTP)
 */
@Service
@Slf4j
public class SendGridApiService {
    
    @Value("${sendgrid.api.key:${SENDGRID}}")
    private String sendGridApiKey;
    
    @Value("${email.from}")
    private String emailFrom;
    
    @PostConstruct
    public void init() {
        log.info("🔧 SendGridApiService initialisé");
        log.info("📧 Email FROM: {}", emailFrom);
        log.info("🔑 API Key configurée: {}", 
                sendGridApiKey != null && !sendGridApiKey.isEmpty() && !sendGridApiKey.contains("${") 
                ? "OUI (longueur: " + sendGridApiKey.length() + " caractères)" 
                : "NON ou INVALIDE");
        if (sendGridApiKey == null || sendGridApiKey.isEmpty() || sendGridApiKey.contains("${")) {
            log.error("❌❌❌ SENDGRID API KEY NON CONFIGURÉE ! Vérifiez la variable d'environnement SENDGRID");
        }
    }
    
    /**
     * Envoie un email HTML via l'API SendGrid
     * 
     * @param toEmail Email du destinataire
     * @param subject Sujet de l'email
     * @param htmlContent Contenu HTML
     * @throws IOException En cas d'erreur API
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws IOException {
        log.info("📧 [SendGrid API] Envoi d'email à: {}", toEmail);
        log.info("📧 [SendGrid API] FROM: {}", emailFrom);
        log.info("📧 [SendGrid API] API Key présente: {}", sendGridApiKey != null && !sendGridApiKey.isEmpty() ? "OUI (longueur: " + sendGridApiKey.length() + ")" : "NON");
        
        try {
            Email from = new Email(emailFrom, "SendByOp");
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            log.info("🔧 [SendGrid API] Construction de la requête...");
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            log.info("🚀 [SendGrid API] Envoi de la requête vers SendGrid...");
            Response response = sg.api(request);
            
            log.info("📥 [SendGrid API] Réponse reçue - Status: {}", response.getStatusCode());
            log.info("📥 [SendGrid API] Body: {}", response.getBody());
            log.info("📥 [SendGrid API] Headers: {}", response.getHeaders());
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ [SendGrid API] Email envoyé avec succès à: {} (Status: {})", 
                        toEmail, response.getStatusCode());
            } else {
                log.error("❌ [SendGrid API] Erreur lors de l'envoi (Status: {}): {}", 
                        response.getStatusCode(), response.getBody());
                throw new IOException("SendGrid API error: " + response.getBody());
            }
            
        } catch (IOException e) {
            log.error("❌ [SendGrid API] Exception IOException: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("❌ [SendGrid API] Exception inattendue: {}", e.getMessage(), e);
            throw new IOException("Erreur inattendue: " + e.getMessage(), e);
        }
    }
    
    /**
     * Envoie un email texte simple
     */
    public void sendTextEmail(String toEmail, String subject, String textContent) throws IOException {
        log.info("📧 [SendGrid API] Envoi d'email texte à: {}", toEmail);
        
        Email from = new Email(emailFrom, "SendByOp");
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", textContent);
        
        Mail mail = new Mail(from, subject, to, content);
        
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sg.api(request);
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            log.info("✅ [SendGrid API] Email texte envoyé avec succès à: {}", toEmail);
        } else {
            log.error("❌ [SendGrid API] Erreur (Status: {}): {}", 
                    response.getStatusCode(), response.getBody());
            throw new IOException("SendGrid API error: " + response.getBody());
        }
    }
}
