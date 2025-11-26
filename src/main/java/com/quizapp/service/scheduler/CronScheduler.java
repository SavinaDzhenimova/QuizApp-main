package com.quizapp.service.scheduler;

import com.quizapp.model.entity.User;
import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.UserService;
import com.quizapp.service.interfaces.UserStatisticsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CronScheduler {

    private final Logger LOGGER = LoggerFactory.getLogger(CronScheduler.class);
    private final PasswordResetService passwordResetService;
    private final GuestQuizService guestQuizService;
    private final UserStatisticsService userStatisticsService;
    private final UserService userService;

    @Scheduled(cron = "0 20 16 * * *")
    @Transactional
    public void cleanUpTokens() {
        LocalDateTime now = LocalDateTime.now();

        this.passwordResetService.deleteInvalidPasswordResetTokens(now);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formatted = now.format(formatter);
        this.LOGGER.info("Невалидните токени са премахнати в " + formatted);
    }

    @Scheduled(fixedRate = 900_000)
    @Transactional
    public void cleanUpGuestQuizzes() {
        LocalDateTime now = LocalDateTime.now();

        this.guestQuizService.deleteExpiredGuestQuizzes(now);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formatted = now.format(formatter);
        this.LOGGER.info("Изтеклите куизове са премахнати в " + formatted);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void runCleanup() {
        LocalDateTime dateTime = LocalDateTime.now().minusYears(1);

        List<User> inactiveUsers = this.userStatisticsService.findInactiveUsers(dateTime);

        if (inactiveUsers.isEmpty()) {
            return;
        }

        inactiveUsers.stream()
                .map(User::getId)
                .forEach(this.userService::deleteById);
    }
}