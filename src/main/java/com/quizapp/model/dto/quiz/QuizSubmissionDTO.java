package com.quizapp.model.dto.quiz;

import com.quizapp.model.annotations.AllQuestionsAnswered;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionDTO {

    @NotBlank
    private String viewToken;

    @AllQuestionsAnswered
    private Map<Long, String> answers;
}