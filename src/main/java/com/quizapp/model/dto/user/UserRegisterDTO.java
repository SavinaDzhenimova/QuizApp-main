package com.quizapp.model.dto.user;

import com.quizapp.model.annotations.ValidEmail;
import com.quizapp.model.annotations.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {

    @NotBlank(message = "Моля въведете потребителско име!")
    @Size(min = 4, max = 15, message = "Потребителското име трябва да бъде между 4 и 15 символа!")
    private String username;

    @ValidEmail(message = "Имейлът трябва да бъде във формат example@domain.com")
    private String email;

    @ValidPassword(message = "Паролата трябва да бъде между 8 и 20 символа и да съдържа поне една главна буква и поне една цифра!")
    private String password;

    @NotBlank(message = "Потвърждението на паролата е задължително!")
    private String confirmPassword;
}