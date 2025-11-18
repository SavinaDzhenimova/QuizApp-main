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
            QuestionApiDTO questionApiDTO = this.makeGetRequest(id);

            return this.mapQuestionApiToDTO(questionApiDTO);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public Result addQuestion(AddQuestionDTO addQuestionDTO) {
        try {
            ResponseEntity<Void> response = this.makePostRequest(addQuestionDTO);
            ApiResponse apiResponse = ApiResponse.fromStatus(response.getStatusCode());

            if (apiResponse.equals(ApiResponse.SUCCESS)) {
                return new Result(true, "Успешно добавихте въпрос.");
            }

            return new Result(false, "Сървърна грешка при добавяне на въпрос!");
        } catch (HttpClientErrorException.NotFound e) {

            return new Result(false, "Не е намерена категория!");
        } catch (HttpClientErrorException.BadRequest e) {

            return new Result(false, "Невалидни входни данни.");
        } catch (HttpClientErrorException e) {

            return new Result(false, "Нещо се обърка! Въпросът не беше записан.");
        }
    }

    @Override
    public Result updateQuestion(Long id, UpdateQuestionDTO updateQuestionDTO) {
        try {
            ResponseEntity<Void> response = this.makePutRequest(id, updateQuestionDTO);
            ApiResponse apiResponse = ApiResponse.fromStatus(response.getStatusCode());

            return switch (apiResponse) {
                case SUCCESS ->  new Result(true, "Успешно редактирахте въпрос.");
                case NO_CONTENT -> new Result(false, "Няма промени за запазване!");
                default -> new Result(false, "Сървърна грешка при редактиране!");
            };

        } catch (HttpClientErrorException.NotFound e) {

            return new Result(false, "Въпросът не е намерен!");
        } catch (HttpClientErrorException e) {

            return new Result(false, "Грешка при редактиране! Въпросът не можа да бъде променен.");
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