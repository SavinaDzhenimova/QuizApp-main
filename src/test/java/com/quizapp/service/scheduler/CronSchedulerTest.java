package com.quizapp.service.scheduler;

import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CronSchedulerTest {

    @Mock
    private PasswordResetService mockPasswordResetService;
    @Mock
    private GuestQuizService mockGuestQuizService;
    @Mock
    private UserService mockUserService;
    @InjectMocks
    private CronScheduler cronScheduler;

    @Test
    void cleanUpTokens_ShouldCallPasswordResetService() {
        when(this.mockPasswordResetService.deleteInvalidPasswordResetTokens())
                .thenReturn("11.12.2025 16:20");

        this.cronScheduler.cleanUpTokens();

        verify(this.mockPasswordResetService, times(1))
                .deleteInvalidPasswordResetTokens();
    }

    @Test
    void sendDeletionWarningEmails_ShouldCallUserService() {
        when(this.mockUserService.sendInactiveUsersWarnEmail())
                .thenReturn(24);

        this.cronScheduler.sendDeletionWarningEmails();

        verify(this.mockUserService, times(1))
                .sendInactiveUsersWarnEmail();
    }

    @Test
    void cleanupInactiveUsersWarned_ShouldCallUserService() {
        when(this.mockUserService.removeWarnedInactiveLoginUsersAccounts())
                .thenReturn(24);

        this.cronScheduler.cleanupInactiveUsersWarned();

        verify(this.mockUserService, times(1))
                .removeWarnedInactiveLoginUsersAccounts();
    }

    @Test
    void sendInactiveUsersReminderEmails_ShouldCallUserService() {
        when(this.mockUserService.sendInactiveSolvingQuizzesUsersEmails())
                .thenReturn(24);

        this.cronScheduler.sendInactiveUsersReminderEmails();

        verify(this.mockUserService, times(1))
                .sendInactiveSolvingQuizzesUsersEmails();
    }

    @Test
    void resendWarnedInactiveUsersReminderEmails_ShouldCallUserService() {
        when(this.mockUserService.resendWarnedInactiveSolvingQuizzesUsersEmails())
                .thenReturn(24);

        this.cronScheduler.resendWarnedInactiveUsersReminderEmails();

        verify(this.mockUserService, times(1))
                .resendWarnedInactiveSolvingQuizzesUsersEmails();
    }
}