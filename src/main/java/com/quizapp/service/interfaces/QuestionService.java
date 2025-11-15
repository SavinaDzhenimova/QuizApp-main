package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.UpdateQuestionDTO;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.model.entity.Result;

import java.util.List;

public interface QuestionService {

    List<QuestionDTO> getAllQuestions();

    QuestionDTO getQuestionById(Long id);

    Result addQuestion(AddQuestionDTO addQuestionDTO);

    QuestionDTO mapQuestionApiToDTO(QuestionApiDTO questionApiDTO);

    boolean deleteQuestionById(Long id);

    QuestionApiDTO makeGetRequest(Long id);

    QuestionApiDTO[] makeGetRequestByCategoryId(Long categoryId);

    Result updateQuestion(Long id, UpdateQuestionDTO updateQuestionDTO);
}