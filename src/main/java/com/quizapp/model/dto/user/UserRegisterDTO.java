package com.quizapp.model.dto.user;

import com.quizapp.model.annotations.ValidEmail;
import com.quizapp.model.annotations.ValidPassword;
import com.quizapp.model.annotations.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {

    @ValidUsername
    private String username;

    @ValidEmail
    private String email;

    @ValidPassword
    private String password;

    @NotBlank(message = "Потвърждението на паролата е задължително!")
    private String confirmPassword;
}