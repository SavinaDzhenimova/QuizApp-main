package com.quizapp.model.dto.question;

import com.quizapp.model.dto.PageDTO;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPageDTO<T> extends PageDTO {

    private List<T> questions;
}