package com.quizapp.service.scheduler;

import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CronScheduler {

    private final Logger LOGGER = LoggerFactory.getLogger(CronScheduler.class);
    private final PasswordResetService passwordResetService;
    private final GuestQuizService guestQuizService;
    private final UserService userService;

    @Scheduled(cron = "0 20 16 * * *")
    @Transactional
    public void cleanUpTokens() {
        String formattedTime = this.passwordResetService.deleteInvalidPasswordResetTokens();

        this.LOGGER.info("Невалидните токени са премахнати в " + formattedTime);
    }

    @Scheduled(fixedRate = 900_000)
    @Transactional
    public void cleanUpGuestQuizzes() {
        String formattedTime = this.guestQuizService.deleteExpiredGuestQuizzes();

        this.LOGGER.info("Изтеклите куизове са премахнати в " + formattedTime);
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void sendDeletionWarningEmails() {
        this.userService.sendInactiveUsersWarnEmail();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void runCleanup() {
        Integer removedProfiles = this.userService.removeInactiveLoginUsersProfiles();

        this.LOGGER.info("Изтрити са профили на {} неактивни потребители.", removedProfiles);
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void sendInactiveUsersReminderEmails() {
        Integer sentEmailsCount = this.userService.sendInactiveSolvingQuizzesUsersEmails();

        this.LOGGER.info("Изпратени са имейли на {} неактивни потребители.", sentEmailsCount);
    }
}