package com.quizapp.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private EmailServiceImpl mockEmailService;

    @BeforeEach
    void setup() {
        this.mockEmailService = new EmailServiceImpl(templateEngine, javaMailSender, "quizapp@gmail.com");
    }

    @Test
    void sendInquiryEmail_ShouldSendEmailCorrectly() throws Exception {
        String fullName = "John Doe";
        String email = "john@example.com";
        String theme = "Test Theme";
        String message = "Some message here";

        when(templateEngine.process(eq("/email/inquiry-email"), any(Context.class)))
                .thenReturn("<html>email-content</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendInquiryEmail(fullName, email, theme, message);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        MimeMessage sent = messageCaptor.getValue();

        Assertions.assertEquals("savina.dzhenimova@gmail.com", sent.getAllRecipients()[0].toString());
        Assertions.assertEquals("quizapp@gmail.com", sent.getFrom()[0].toString());
        Assertions.assertEquals(email, sent.getReplyTo()[0].toString());
        Assertions.assertEquals("Ново запитване от " + fullName, sent.getSubject());

        String body = (String) sent.getContent();
        Assertions.assertTrue(body.contains("email-content"));
    }

    @Test
    void sendInquiryReceivedEmail_ShouldUseCorrectTemplate() {
        when(templateEngine.process(eq("/email/inquiry-received-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendInquiryReceivedEmail("John Doe", "john@example.com");

        verify(templateEngine).process(eq("/email/inquiry-received-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendForgotPasswordEmail_ShouldUseCorrectTemplate() {
        when(templateEngine.process(eq("/email/forgot-password-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendForgotPasswordEmail("john", "john@example.com", "token123");

        verify(templateEngine).process(eq("/email/forgot-password-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendUserRegisterEmail_ShouldUseCorrectTemplate() {
        when(templateEngine.process(eq("/email/user-register-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendUserRegisterEmail("john", "john@example.com");

        verify(templateEngine).process(eq("/email/user-register-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendAddedAdminEmail_ShouldUseCorrectTemplate() {
        when(this.templateEngine.process(eq("/email/admin-added-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendAddedAdminEmail("john", "john@example.com", "token123");

        verify(templateEngine).process(eq("/email/admin-added-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendReportProblemEmail_ShouldUseCorrectTemplate() {
        when(this.templateEngine.process(eq("/email/report-problem-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendReportProblemEmail("John Doe", "john@example.com", "problem", "identifier", "description");

        verify(templateEngine).process(eq("/email/report-problem-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendProblemReceivedEmail_ShouldUseCorrectTemplate() {
        when(this.templateEngine.process(eq("/email/problem-report-received-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendProblemReceivedEmail("John Doe", "john@example.com", "problem");

        verify(templateEngine).process(eq("/email/problem-report-received-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendSubscribeEmail_ShouldUseCorrectTemplate() {
        when(this.templateEngine.process(eq("/email/subscribe-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendSubscribeEmail("john@example.com");

        verify(templateEngine).process(eq("/email/subscribe-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendInactiveSolvingQuizzesEmail_ShouldUseCorrectTemplate() {
        when(this.templateEngine.process(eq("/email/inactive-user-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendInactiveSolvingQuizzesEmail("john", "john@example.com");

        verify(templateEngine).process(eq("/email/inactive-user-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendInactiveUserNotWarnedEmail_ShouldUseCorrectTemplate() {
        when(this.templateEngine.process(eq("/email/inactive-user-warning-email"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        this.mockEmailService.sendInactiveUserNotWarnedEmail("john", "john@example.com");

        verify(templateEngine).process(eq("/email/inactive-user-warning-email"), any(Context.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }
}