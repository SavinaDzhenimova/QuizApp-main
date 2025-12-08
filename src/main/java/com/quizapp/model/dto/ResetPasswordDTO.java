package com.quizapp.model.dto;

import com.quizapp.model.annotations.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordDTO {

    @ValidPassword
    private String password;

    @NotBlank(message = "Потвърждението на паролата е задължително!")
    private String confirmPassword;

    @NotBlank
    private String token;
}