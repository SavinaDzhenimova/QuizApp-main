package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.entity.Question;

import java.util.List;

public interface QuestionService {
    List<QuestionDTO> getAllQuestions();

    QuestionDTO getQuestionById(Long id);

    Object addQuestion(AddQuestionDTO addQuestionDTO);

    boolean deleteQuestionById(Long id);

    Question makeGetRequest(Long id);

    Question[] makeGetRequestAll();

    Question makePostRequest(AddQuestionDTO addQuestionDTO);

    void makeDeleteRequest(Long id);
}
