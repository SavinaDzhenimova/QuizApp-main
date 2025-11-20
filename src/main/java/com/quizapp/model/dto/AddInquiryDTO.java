package com.quizapp.model.dto;

import com.quizapp.model.annotations.ValidEmail;
import jakarta.validation.constraints.NotBlank;
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
    private String fullName;

    @ValidEmail
    private String email;

    @NotBlank(message = "Въведете тема на запитването.")
    private String theme;

    @NotBlank(message = "Въведете запитване.")
    private String message;
}