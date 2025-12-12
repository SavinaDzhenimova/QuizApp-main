package com.quizapp.service;

import com.quizapp.model.dto.ReportProblemDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.enums.ProblemType;
import com.quizapp.service.events.ReportProblemEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportProblemServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private ReportProblemServiceImpl reportProblemService;

    private ReportProblemDTO reportProblemDTO;

    @BeforeEach
    void setUp() {
        this.reportProblemDTO = ReportProblemDTO.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .problemType(ProblemType.WRONG_ANSWER)
                .questionIdentifier("Identifier")
                .description("Description")
                .build();
    }

    @Test
    void sendEmailToReportProblem_ShouldReturnError_WhenDataNotValid() {
        Result result = this.reportProblemService.sendEmailToReportProblem(null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Невалидни входни данни.", result.getMessage());
        verify(this.eventPublisher, never()).publishEvent(any(ReportProblemEvent.class));
    }

    @Test
    void sendEmailToReportProblem_ShouldSendEmail_WhenDataIsValid() {
        Result result = this.reportProblemService.sendEmailToReportProblem(this.reportProblemDTO);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Докладът Ви за проблем беше изпратен успешно!", result.getMessage());
        verify(this.eventPublisher, times(1)).publishEvent(any(ReportProblemEvent.class));
    }
}