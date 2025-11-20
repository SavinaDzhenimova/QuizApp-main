package com.quizapp.service;

import com.quizapp.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final String email;

    public EmailServiceImpl(TemplateEngine templateEngine, JavaMailSender javaMailSender,
                            @Value("${mail.quiz_app}") String email) {
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
        this.email = email;
    }

    @Override
    public void sendInquiryEmail(String fullName, String email, String theme, String message) {
        Map<String, Object> variables = Map.of(
                "fullName", fullName,
                "email", email,
                "theme", theme,
                "message", message
        );

        String content = generateEmailContent("/email/inquiry-email", variables);
        sendEmail("savina.dzhenimova@gmail.com", "Ново запитване от " + fullName, content, email);
    }

    @Override
    public void sendForgotPasswordEmail(String fullName, String email, String token) {
        Map<String, Object> variables = Map.of(
                "fullName", fullName,
                "email", email,
                "token", token
        );

        String content = generateEmailContent("/email/forgot-password-email", variables);
        sendEmail(email, "Линк за промяна на парола", content, this.email);
    }

    @Override
    public void sendUserRegisterEmail(String fullName, String email, String phoneNumber) {
        Map<String, Object> variables = Map.of(
                "fullName", fullName,
                "email", email,
                "phoneNumber", phoneNumber
        );

        String content = generateEmailContent("/email/user-register-email", variables);
        sendEmail(email, "Успешна регистрация в Runtastic Shoes", content, this.email);
    }

    @Override
    public void sendReportBugEmail(String fullName, String email, String phoneNumber, String address, String requestType) {
        Map<String, Object> variables = Map.of(
                "fullName", fullName,
                "email", email,
                "phoneNumber", phoneNumber,
                "address", address,
                "requestType", requestType
        );

        String content = generateEmailContent("/email/make-request-email", variables);
        sendEmail("savina.dzhenimova@gmail.com", "Заявка за " + requestType, content, this.email);
    }

    @Override
    public void sendSubscribeEmail(String email) {
        Map<String, Object> variables = Map.of(
                "email", email
        );

        String content = generateEmailContent("/email/subscribe-email", variables);
        sendEmail(email, "Успешен абонамент", content, this.email);
    }

    private void sendEmail(String sendTo, String subject, String content, String replyTo) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setTo(sendTo);
            mimeMessageHelper.setFrom(this.email);
            mimeMessageHelper.setReplyTo(replyTo);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Грешка при изпращане на имейл: " + e.getMessage(), e);
        }
    }

    private String generateEmailContent(String templatePath, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templatePath, context);
    }
}