package com.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemDetailDTO {

    private int status;

    private String title;

    private String detail;

    private String type;

    private String instance;

    private String code;

    private String timestamp;
}