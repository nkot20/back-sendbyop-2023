package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendMailService {
    private final JavaMailSender javaMailSender;
    @Value("${email.from}")
    private String emailFrom;

    public EmailDto sendEmail(@Validated EmailDto email) {
        System.out.println("sending email");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(emailFrom);
        simpleMailMessage.setTo(email.getTo());
        simpleMailMessage.setSubject(email.getTopic());
        simpleMailMessage.setText(email.getBody());
        javaMailSender.send(simpleMailMessage);
        System.out.println("sent email");
        return email;
    }

    public void sendListEmail(List<EmailDto> emails) {
        for (EmailDto email: emails
             ) {
            sendEmail(email);
        }
    }

    /**
     * Envoie un email HTML
     * 
     * @param toEmail Email du destinataire
     * @param subject Sujet de l'email
     * @param htmlContent Contenu HTML de l'email
     * @throws MessagingException En cas d'erreur d'envoi
     * @throws UnsupportedEncodingException En cas d'erreur d'encodage
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent)
            throws MessagingException, UnsupportedEncodingException {
        log.info("Envoi d'un email HTML à {}", toEmail);
        
        try {
            String senderName = "SendByOp";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            helper.setFrom(emailFrom, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Email HTML envoyé avec succès à {}", toEmail);
        } catch (MessagingException e) {
            log.error("Erreur MessagingException lors de l'envoi de l'email à {} : {}", 
                     toEmail, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'envoi de l'email à {} : {}", 
                     toEmail, e.getMessage(), e);
            throw new MessagingException("Erreur lors de l'envoi de l'email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Envoie un email HTML avec une pièce jointe
     */
    public void sendEmailWithAttachment(String toEmail, String subject, String htmlContent, 
                                       byte[] attachment, String attachmentName) 
            throws MessagingException, UnsupportedEncodingException {
        log.info("Envoi d'un email avec pièce jointe à {}", toEmail);
        
        try {
            String senderName = "SendByOp";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(emailFrom, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Ajouter la pièce jointe
            if (attachment != null && attachment.length > 0) {
                helper.addAttachment(attachmentName, () -> new java.io.ByteArrayInputStream(attachment));
                log.info("Pièce jointe ajoutée: {} ({} bytes)", attachmentName, attachment.length);
            }

            javaMailSender.send(message);
            log.info("Email avec pièce jointe envoyé avec succès à {}", toEmail);
        } catch (MessagingException e) {
            log.error("Erreur MessagingException lors de l'envoi de l'email à {} : {}", 
                     toEmail, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'envoi de l'email à {} : {}", 
                     toEmail, e.getMessage(), e);
            throw new MessagingException("Erreur lors de l'envoi de l'email: " + e.getMessage(), e);
        }
    }

    /**
     * @deprecated Utiliser sendHtmlEmail() avec EmailTemplateService à la place
     */
    @Deprecated
    public void sendVerificationEmail(CustomerDto user, String siteURL, String token, String header, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        log.warn("Méthode dépréciée sendVerificationEmail() appelée. Utiliser sendHtmlEmail() avec EmailTemplateService.");
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    public void simpleHtmlMessage(CustomerDto user, String content, String subject) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String senderName = "SendByOp";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailFrom, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getLastName() + user.getFirstName());

        content = content.replace("[[URLSENDBYOP]]", "www.sendbyop.com");

        helper.setText(content, true);

        System.out.println("send email....");
        javaMailSender.send(message);
        System.out.println("Email is send !");
    }
}
