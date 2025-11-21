package com.quizapp.model.dto;

import com.quizapp.model.annotations.ValidEmail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeDTO {

    @ValidEmail
    private String email;
}