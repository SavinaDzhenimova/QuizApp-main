package com.quizapp.service.scheduler;

import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableScheduling
@ActiveProfiles("test")
public class CronSchedulerSpringTest {

    @Autowired
    private ScheduledAnnotationBeanPostProcessor processor;
    @Autowired
    private CronScheduler cronScheduler;

    @MockitoBean
    private GuestQuizService mockGuestQuizService;
    @MockitoBean
    private PasswordResetService mockPasswordResetService;
    @MockitoBean
    private UserService mockUserService;

    @Test
    void triggerScheduledTasks() {
        this.processor.postProcessAfterInitialization(this.cronScheduler, "cronScheduler");

        this.processor.getScheduledTasks().forEach(task -> task.getTask().getRunnable().run());

        verify(this.mockPasswordResetService, atLeastOnce()).deleteInvalidPasswordResetTokens();
        verify(this.mockGuestQuizService, atLeastOnce()).deleteExpiredGuestQuizzes();
        verify(this.mockUserService, atLeastOnce()).sendInactiveUsersWarnEmail();
        verify(this.mockUserService, atLeastOnce()).removeWarnedInactiveLoginUsersAccounts();
        verify(this.mockUserService, atLeastOnce()).sendInactiveSolvingQuizzesUsersEmails();
        verify(this.mockUserService, atLeastOnce()).resendWarnedInactiveSolvingQuizzesUsersEmails();
    }
}