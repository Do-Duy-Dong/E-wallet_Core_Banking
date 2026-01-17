package com.example.user_service.service;

import com.example.user_service.dto.MailVariable;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(MailVariable mailVariable) {
        Context context= new Context();
        context.setVariables(Map.of(
                "name",mailVariable.getName(),
                "otp",mailVariable.getOtp(),
                "subject",mailVariable.getSubject()
        ));
        String htmlBody= templateEngine.process("mail",context);
        try{
            MimeMessage mimeMessage= mailSender.createMimeMessage();
            MimeMessageHelper helper= new MimeMessageHelper(mimeMessage,true, StandardCharsets.UTF_8.name());
            helper.setTo(mailVariable.getEmail());
            helper.setSubject(mailVariable.getSubject());
            helper.setText(htmlBody,true);
            mailSender.send(mimeMessage);
            log.info("Email sent to {}", mailVariable.getEmail());
        }catch (Exception e){
            e.printStackTrace();
            log.error("Failed to send email to {}", mailVariable.getEmail());
        }

    }

}
