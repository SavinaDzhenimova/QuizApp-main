package com.quizapp.service.scheduler;

import com.quizapp.service.interfaces.GuestQuizService;
import com.quizapp.service.interfaces.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CronScheduler {

    private final Logger LOGGER = LoggerFactory.getLogger(CronScheduler.class);
    private final PasswordResetService passwordResetService;
    private final GuestQuizService guestQuizService;

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
}