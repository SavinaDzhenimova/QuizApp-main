package com.quizapp.service.utils;

import com.quizapp.model.dto.quiz.QuizDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class GuestQuizStorageTest {

    @InjectMocks
    private GuestQuizStorage mockQuestQuizStorage;

    private QuizDTO quizDTO;

    @BeforeEach
    void setUp() {
        this.quizDTO = QuizDTO.builder()
                .viewToken("token123")
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    void get_ShouldReturnNull_WhenQuizNotFound() {
        QuizDTO dto = this.mockQuestQuizStorage.get("missing");

        Assertions.assertNull(dto);
    }

    @Test
    void get_ShouldReturnQuizDTO_WhenQuizFound() {
        this.mockQuestQuizStorage.put("token123", this.quizDTO);

        QuizDTO result = this.mockQuestQuizStorage.get("token123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.quizDTO.getViewToken(), result.getViewToken());
    }

    @Test
    void remove_ShouldRemoveQuiz() {
        this.mockQuestQuizStorage.put("token123", this.quizDTO);

        this.mockQuestQuizStorage.remove("token123");

        Assertions.assertNull(this.mockQuestQuizStorage.get("token123"));
    }

    @Test
    void deleteExpiredQuizzes_ShouldDeleteExpiredQuizzes() {
        QuizDTO expiredQuiz = QuizDTO.builder()
                .viewToken("expired")
                .expireAt(LocalDateTime.now().minusMinutes(5))
                .build();

        this.mockQuestQuizStorage.put("token123", quizDTO);
        this.mockQuestQuizStorage.put("expired", expiredQuiz);

        this.mockQuestQuizStorage.deleteExpiredQuizzes(LocalDateTime.now());

        Assertions.assertNotNull(this.mockQuestQuizStorage.get("token123"));
        Assertions.assertNull(this.mockQuestQuizStorage.get("expired"));
    }
}