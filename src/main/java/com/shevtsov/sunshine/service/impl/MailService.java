package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.time.LocalDateTime;


@Component
@Slf4j
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    private VelocityEngine velocityEngine;

    public void sendActivationEmail(User user) {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "utf-8");
        try {
            helper.setFrom(username);
            helper.setTo(user.getUserInfo().getEmail());
            helper.setSubject("Account activation");

            VelocityContext context = new VelocityContext();
            context.put("username", user.getUsername());
            context.put("activationCode", user.getActivationCode());
            StringWriter stringWriter = new StringWriter();
            try {
                velocityEngine.mergeTemplate("activation.vm", "UTF-8", context, stringWriter);
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }
            String text = stringWriter.toString();
            helper.setText(text, true);
            mailSender.send(mailMessage);
            log.info("Email sent! To: " + user.getUsername() + ". At: " + user.getUserInfo().getEmail() + ". Time: " + LocalDateTime.now());
        } catch (MessagingException ex) {
            log.error(ex.getMessage());
        }
    }

    public void sendForgotPasswordEmail(User user) {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "utf-8");
        try {
            helper.setFrom(username);
            helper.setTo(user.getUserInfo().getEmail());
            helper.setSubject("Password restore");

            VelocityContext context = new VelocityContext();
            context.put("username", user.getUsername());
            context.put("restoreCode", user.getRestorePasswordCode());
            StringWriter stringWriter = new StringWriter();
            try {
                velocityEngine.mergeTemplate("forgotPassword.vm", "UTF-8", context, stringWriter);
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }
            String text = stringWriter.toString();
            helper.setText(text, true);
            mailSender.send(mailMessage);
            log.info("Email sent! To: " + user.getUsername() + ". At: " + user.getUserInfo().getEmail() + ". Time: " + LocalDateTime.now());
        } catch (MessagingException ex) {
            log.error(ex.getMessage());
        }
    }

    public void sendAnswerEmail(User user, String question, String answer) {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "utf-8");
        try {
            helper.setFrom(username);
            helper.setTo(user.getUserInfo().getEmail());
            helper.setSubject("Support request answer");

            VelocityContext context = new VelocityContext();
            context.put("username", user.getUsername());
            context.put("question", question);
            context.put("answer", answer);
            StringWriter stringWriter = new StringWriter();
            try {
                velocityEngine.mergeTemplate("supportAnswer.vm", "UTF-8", context, stringWriter);
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }
            String text = stringWriter.toString();
            helper.setText(text, true);
            mailSender.send(mailMessage);
            log.info("Email sent! To: " + user.getUsername() + ". At: " + user.getUserInfo().getEmail() + ". Time: " + LocalDateTime.now());
        } catch (MessagingException ex) {
            log.error(ex.getMessage());
        }
    }
}
