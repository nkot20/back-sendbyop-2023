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

    public void sendVerificationEmail(CustomerDto user, String siteURL, String token, String header, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        try {
            log.info("Envoi de l'email de vérification à {}", user.getEmail());
            String toAddress = user.getEmail();
            String senderName = "SendByOp";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(emailFrom, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);

            content = content.replace("[[name]]", user.getLastName() + user.getFirstName());
            String verifyURL = siteURL + header + token;

            content = content.replace("[[URL]]", verifyURL);

            content = content.replace("[[URLSENDBYOP]]", "www.sendbyop.com");

            helper.setText(content, true);

            System.out.println("send email....");
            javaMailSender.send(message);
            System.out.println("Email is send !");
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de vérification à {} : {}", user.getEmail(), e.getMessage(), e);
        }


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
