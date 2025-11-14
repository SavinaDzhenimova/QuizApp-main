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
public class UpdateCategoryDTO {

    private Long id;

    private String name;

    @NotBlank(message = "Въведете описание за категорията!")
    private String description;
}