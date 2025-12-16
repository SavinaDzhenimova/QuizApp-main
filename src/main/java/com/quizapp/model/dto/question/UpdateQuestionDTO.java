package com.quizapp.model.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQuestionDTO {

    @NotNull
    private Long id;

    @NotBlank
    private String questionText;

    @NotBlank
    private String categoryName;

    @NotBlank
    private String correctAnswer;

    @NotBlank
    private String options;
}