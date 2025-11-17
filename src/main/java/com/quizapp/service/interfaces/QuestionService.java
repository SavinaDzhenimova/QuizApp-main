package com.quizapp.service.interfaces;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuestionPageDTO;
import com.quizapp.model.dto.UpdateQuestionDTO;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.model.entity.Result;
import org.springframework.data.domain.Pageable;

public interface QuestionService {

    QuestionPageDTO<QuestionDTO> getAllQuestions(String questionText, Long categoryId, Pageable pageable);

    QuestionDTO getQuestionById(Long id);

    Result addQuestion(AddQuestionDTO addQuestionDTO);

    QuestionDTO mapQuestionApiToDTO(QuestionApiDTO questionApiDTO);

    boolean deleteQuestionById(Long id);

    QuestionApiDTO makeGetRequest(Long id);

    QuestionApiDTO[] makeGetRequestByCategoryId(Long categoryId);

    Result updateQuestion(Long id, UpdateQuestionDTO updateQuestionDTO);
}