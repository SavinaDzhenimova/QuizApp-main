package com.quizapp.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQuestionDTO {

    private Long id;

    private String questionText;

    private String categoryName;

    private String correctAnswer;

    private String options;
}