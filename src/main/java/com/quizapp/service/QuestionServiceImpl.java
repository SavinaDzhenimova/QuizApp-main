package com.quizapp.service;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.QuestionPageDTO;
import com.quizapp.model.dto.UpdateQuestionDTO;
import com.quizapp.model.enums.ApiResponse;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final RestClient restClient;

    @Override
    public QuestionPageDTO<QuestionDTO> getAllQuestions(String questionText, Long categoryId, Pageable pageable) {
        QuestionPageDTO<QuestionApiDTO> pageDTO = this.makeGetRequestAll(questionText, categoryId,
                pageable.getPageNumber(), pageable.getPageSize());

        List<QuestionDTO> questionDTOs = pageDTO.getQuestions().stream()
                .map(this::mapQuestionApiToDTO)
                .toList();

        return QuestionPageDTO.<QuestionDTO>builder()
                .questions(questionDTOs)
                .totalPages(pageDTO.getTotalPages())
                .totalElements(pageDTO.getTotalElements())
                .currentPage(pageDTO.getCurrentPage())
                .size(pageDTO.getSize())
                .build();
    }

    @Override
    public QuestionDTO getQuestionById(Long id) {
        try {
            QuestionApiDTO questionApiDTO = this.makeGetRequest(id);

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

            return new Result(false, "Нещо се обърка! Въпросът не беше записан.");
        }
    }

    @Override
    public Result updateQuestion(Long id, UpdateQuestionDTO updateQuestionDTO) {
        try {
            QuestionApiDTO questionApiDTO = this.makeGetRequest(id);

            if (questionApiDTO == null) {
                return new Result(false, "Въпросът не е намерен!");
            }

            List<String> updateOptions = this.stringToList(updateQuestionDTO.getOptions());
            if (questionApiDTO.getQuestionText().equals(updateQuestionDTO.getQuestionText())
                    && questionApiDTO.getCategoryName().equals(updateQuestionDTO.getCategoryName())
                    && questionApiDTO.getCorrectAnswer().equals(updateQuestionDTO.getCorrectAnswer())
                    && new HashSet<>(questionApiDTO.getOptions()).equals(new HashSet<>(updateOptions))) {

                return new Result(false, "Няма промени за запазване!");
            }

            ResponseEntity<Void> response = this.makePutRequest(id, updateQuestionDTO);
            ApiResponse apiResponse = ApiResponse.fromStatus(response.getStatusCode());

            if (apiResponse.equals(ApiResponse.SUCCESS)) {
                return new Result(true, "Успешно редактирахте въпрос.");
            }

            return new Result(false, "Сървърна грешка при редактиране!");
        } catch (HttpClientErrorException e) {

            return new Result(false, "Грешка при редактиране! Въпросът не можа да бъде променен.");
        }
    }

    private List<String> stringToList(String options) {
        return Arrays.stream(options.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
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

    @Override
    public boolean deleteQuestionById(Long id) {
        try {
            this.makeDeleteRequest(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public QuestionApiDTO makeGetRequest(Long id) {
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

    private QuestionApiDTO makePostRequest(AddQuestionDTO addQuestionDTO) {
        return this.restClient.post()
                .uri("/api/questions")
                .body(addQuestionDTO)
                .retrieve()
                .body(QuestionApiDTO.class);
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