package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.dto.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendMailService {
    private final JavaMailSender javaMailSender;

    public EmailDto sendEmail(@Validated EmailDto email) {
        System.out.println("sending email");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("etiennenkot1@gmail.com");
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
        String toAddress = user.getEmail();
        String fromAddress = "etiennenkot1@gmail.com";
        String senderName = "SendByOp";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
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

    }

    public void simpleHtmlMessage(CustomerDto user, String content, String subject) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "etiennenkot1@gmail.com";
        String senderName = "SendByOp";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
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
