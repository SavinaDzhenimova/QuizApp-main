package com.quizapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddQuestionDTO {

    @NotBlank(message = "Въведете съдържание на въпроса!")
    private String questionText;

    @NotBlank(message = "Въведете категория на въпроса!")
    private String category;

    @NotBlank(message = "Въведете правилен отговор на въпроса!")
    private String correctAnswer;

    @NotBlank(message = "Въведете опционални отговори на въпроса!")
    private String options;
}