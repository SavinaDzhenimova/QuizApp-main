package com.quizapp.model.dto;

import com.quizapp.model.annotations.ValidEmail;
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
public class AddInquiryDTO {

    @NotBlank(message = "Въведете име и фамилия.")
    @Size(min = 4, max = 20, message = "Името трябва да бъде между 4 и 20 символа.")
    private String fullName;

    @ValidEmail
    private String email;

    @NotBlank(message = "Въведете тема на запитването.")
    private String theme;

    @NotBlank(message = "Въведете запитване.")
    private String message;
}