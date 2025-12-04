package com.quizapp.model.dto.user;

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
public class UpdatePasswordDTO {

    @NotBlank(message = "Въведете старата си парола.")
    private String oldPassword;

    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Потвърдете новата парола.")
    private String confirmPassword;
}