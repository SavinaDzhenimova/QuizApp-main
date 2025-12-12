package com.quizapp.service;

import com.quizapp.model.dto.ReportProblemDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.events.ReportProblemEvent;
import com.quizapp.service.interfaces.ReportProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportProblemServiceImpl implements ReportProblemService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Result sendEmailToReportProblem(ReportProblemDTO reportProblemDTO) {
        if (reportProblemDTO == null) {
            return new Result(false, "Невалидни входни данни.");
        }

        this.applicationEventPublisher.publishEvent(
                new ReportProblemEvent(this, reportProblemDTO.getFullName(), reportProblemDTO.getEmail(),
                        reportProblemDTO.getProblemType().getDisplayName(), reportProblemDTO.getQuestionIdentifier(),
                        reportProblemDTO.getDescription()));

        return new Result(true, "Докладът Ви за проблем беше изпратен успешно!");
    }
}