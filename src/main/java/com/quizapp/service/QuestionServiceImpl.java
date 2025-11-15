package com.quizapp.service;

import com.quizapp.model.dto.AddQuestionDTO;
import com.quizapp.model.dto.QuestionDTO;
import com.quizapp.model.dto.UpdateQuestionDTO;
import com.quizapp.model.entity.QuestionApiDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final RestClient restClient;

    @Override
    public List<QuestionDTO> getAllQuestions() {
        QuestionApiDTO[] questionApiDTOs = this.makeGetRequestAll();

            return Arrays.stream(questionApiDTOs)
                .map(this::mapQuestionApiToDTO)
                .collect(Collectors.toList());
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

            List<String> updateOptions = this.stringToList(updateQuestionDTO.getOptions());

            if (questionApiDTO.getQuestionText().equals(updateQuestionDTO.getQuestionText())
                || questionApiDTO.getCategoryName().equals(updateQuestionDTO.getCategoryName())
                || questionApiDTO.getCorrectAnswer().equals(updateQuestionDTO.getCorrectAnswer())
                || new HashSet<>(questionApiDTO.getOptions()).equals(new HashSet<>(updateOptions))) {

                return new Result(false, "Няма промени за запазване!");
            }

            this.makePutRequest(id, updateQuestionDTO);

            return new Result(true, "Успешно редактирахте въпрос.");
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

    private QuestionApiDTO[] makeGetRequestAll() {
        return this.restClient.get()
                .uri("/api/questions")
                .retrieve()
                .body(QuestionApiDTO[].class);
    }

    private QuestionApiDTO makePostRequest(AddQuestionDTO addQuestionDTO) {
        return this.restClient.post()
                .uri("/api/questions")
                .body(addQuestionDTO)
                .retrieve()
                .body(QuestionApiDTO.class);
    }

    private QuestionApiDTO makePutRequest(Long id, UpdateQuestionDTO updateQuestionDTO) {
        return this.restClient.put()
                .uri("/api/questions/{id}", id)
                .body(updateQuestionDTO)
                .retrieve()
                .body(QuestionApiDTO.class);
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