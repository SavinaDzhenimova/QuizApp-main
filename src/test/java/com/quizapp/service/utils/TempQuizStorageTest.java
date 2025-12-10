package com.quizapp.service.utils;

import com.quizapp.model.entity.Quiz;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class TempQuizStorageTest {

    @InjectMocks
    private TempQuizStorage mockTempQuizStorage;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        this.quiz = Quiz.builder()
                .viewToken("token123")
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    void get_ShouldReturnNull_WhenQuizNotFound() {
        Quiz quiz = this.mockTempQuizStorage.get("missing");

        Assertions.assertNull(quiz);
    }

    @Test
    void get_ShouldReturnQuizDTO_WhenQuizFound() {
        this.mockTempQuizStorage.put("token123", this.quiz);

        Quiz result = this.mockTempQuizStorage.get("token123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.quiz.getViewToken(), result.getViewToken());
    }

    @Test
    void remove_ShouldRemoveQuiz() {
        this.mockTempQuizStorage.put("token123", this.quiz);

        this.mockTempQuizStorage.remove("token123");

        Assertions.assertNull(this.mockTempQuizStorage.get("token123"));
    }
}