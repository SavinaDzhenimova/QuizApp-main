package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.entity.Question;
import com.quizapp.model.entity.Result;

import java.util.List;

public interface QuestionService {

    List<QuestionDTO> getAllQuestions();

    QuestionDTO getQuestionById(Long id);

    QuestionDTO questionToDTO(Question question);

    Result addQuestion(AddQuestionDTO addQuestionDTO);

    boolean deleteQuestionById(Long id);

    Question makeGetRequest(Long id);

    Question[] makeGetRequestByCategoryId(Long categoryId);
}