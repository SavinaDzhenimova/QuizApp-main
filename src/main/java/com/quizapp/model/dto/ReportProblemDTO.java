package com.quizapp.model.dto;

import com.quizapp.model.annotations.ValidEmail;
import com.quizapp.model.enums.ProblemType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportProblemDTO {

    @NotBlank(message = "Моля въведете име и фамилия.")
    @Size(max = 20, message = "Името не трябва да бъде над 20 символа.")
    private String fullName;

    @ValidEmail
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Моля изберете вид на проблема.")
    private ProblemType problemType;

    private String questionIdentifier;

    @NotBlank(message = "Моля въведете описание на проблема.")
    @Size(min = 10, message = "Описанието трябва да съдържа поне 10 символа.")
    private String description;
}