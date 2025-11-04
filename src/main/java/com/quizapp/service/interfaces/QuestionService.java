package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;

import java.util.List;

public interface QuestionService {
    List<QuestionDTO> getAllQuestions();

    QuestionDTO getQuestionById(Long id);

    Object addQuestion(AddQuestionDTO addQuestionDTO);

    boolean deleteQuestionById(Long id);
}
