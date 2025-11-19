package com.quizapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.model.dto.*;
import com.quizapp.model.rest.QuestionApiDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final RestClient restClient;

    @Override
    public QuestionPageDTO<QuestionDTO> getAllQuestions(String questionText, Long categoryId, Pageable pageable) {
        QuestionPageDTO<QuestionApiDTO> questionPageDTO = this.makeGetRequestAll(questionText, categoryId,
                pageable.getPageNumber(), pageable.getPageSize());

        List<QuestionDTO> questionDTOs = questionPageDTO.getQuestions().stream()
                .map(this::mapQuestionApiToDTO)
                .toList();

        QuestionPageDTO<QuestionDTO> questionPage = new QuestionPageDTO<>();
        questionPage.setQuestions(questionDTOs);
        questionPage.setTotalPages(questionPageDTO.getTotalPages());
        questionPage.setTotalElements(questionPageDTO.getTotalElements());
        questionPage.setCurrentPage(questionPageDTO.getCurrentPage());
        questionPage.setSize(questionPageDTO.getSize());

        return questionPage;
    }

    @Override
    public QuestionDTO getQuestionById(Long id) {
        try {
            QuestionApiDTO questionApiDTO = this.makeGetRequestById(id);

            return this.mapQuestionApiToDTO(questionApiDTO);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public Result addQuestion(AddQuestionDTO addQuestionDTO) {
        try {
            this.makePostRequest(addQuestionDTO);
            return new Result(true, "Успешно добавихте въпрос.");

        } catch (HttpClientErrorException e) {
            String errorMessage = this.extractErrorMessage(e);
            return new Result(false, errorMessage);
        }
    }

    @Override
    public Result updateQuestion(Long id, UpdateQuestionDTO updateQuestionDTO) {
        try {
            this.makePutRequest(id, updateQuestionDTO);
            return new Result(true, "Успешно редактирахте въпрос.");

        } catch (HttpClientErrorException e) {
            String errorMessage = this.extractErrorMessage(e);
            return new Result(false, errorMessage);
        }
    }

    @Override
    public QuestionDTO mapQuestionApiToDTO(QuestionApiDTO questionApiDTO) {
        return QuestionDTO.builder()
                .id(questionApiDTO.getId())
                .categoryName(questionApiDTO.getCategoryName())
                .questionText(questionApiDTO.getQuestionText())
                .correctAnswer(questionApiDTO.getCorrectAnswer())
                .options(questionApiDTO.getOptions())
                .build();
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            ProblemDetailDTO problem = new ObjectMapper().readValue(body, ProblemDetailDTO.class);

            return problem.getDetail();
        } catch (Exception ex) {
            return "Грешка при извикване на REST API";
        }
    }

    @Override
    public QuestionApiDTO makeGetRequestById(Long id) {
        return this.restClient.get()
                .uri("/api/questions/{id}", id)
                .retrieve()
                .body(QuestionApiDTO.class);
    }

    public QuestionPageDTO<QuestionApiDTO> makeGetRequestAll(String questionText, Long categoryId, int page, int size) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/api/questions")
                .queryParam("page", page)
                .queryParam("size", size);

        if (questionText != null && !questionText.isBlank()) {
            uriBuilder.queryParam("questionText", questionText);
        }

        if (categoryId != null) {
            uriBuilder.queryParam("categoryId", categoryId);
        }

        return this.restClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private ResponseEntity<Void> makePostRequest(AddQuestionDTO addQuestionDTO) {
        return this.restClient.post()
                .uri("/api/questions")
                .body(addQuestionDTO)
                .retrieve()
                .toBodilessEntity();
    }

    private ResponseEntity<Void> makePutRequest(Long id, UpdateQuestionDTO updateQuestionDTO) {
        return this.restClient.put()
                .uri("/api/questions/{id}", id)
                .body(updateQuestionDTO)
                .retrieve()
                .toBodilessEntity();
    }

    private void makeDeleteRequest(Long id) {
        this.restClient.delete()
                .uri("/api/questions/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public QuestionApiDTO[] makeGetRequestByCategoryId(Long categoryId) {
        return this.restClient.get()
                .uri("/api/questions/category/{id}", categoryId)
                .retrieve()
                .body(QuestionApiDTO[].class);
    }
}