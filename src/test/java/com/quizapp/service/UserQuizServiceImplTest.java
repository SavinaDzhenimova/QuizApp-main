package com.quizapp.service;

import com.quizapp.exception.QuizNotFoundException;
import com.quizapp.exception.UserNotFoundException;
import com.quizapp.model.dto.question.QuestionDTO;
import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.quiz.QuizResultDTO;
import com.quizapp.model.dto.quiz.QuizSubmissionDTO;
import com.quizapp.model.entity.Quiz;
import com.quizapp.model.entity.SolvedQuiz;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.repository.SolvedQuizRepository;
import com.quizapp.service.interfaces.*;
import com.quizapp.service.utils.TempQuizStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserQuizServiceImplTest {

    @Mock
    private TempQuizStorage mockTempQuizStorage;
    @Mock
    private QuestionService mockQuestionService;
    @Mock
    private CategoryService mockCategoryService;
    @Mock
    private SolvedQuizRepository mockSolvedQuizRepository;
    @Mock
    private UserService mockUserService;
    @Mock
    private UserStatisticsService mockUserStatsService;
    @Mock
    private CategoryStatisticsService mockCategoryStatsService;
    @Mock
    private QuestionStatisticsService mockQuestionStatsService;
    @InjectMocks
    private UserQuizServiceImpl mockUserQuizService;

    private Quiz mockQuiz;
    private SolvedQuiz solvedQuiz;
    private QuestionDTO questionDTO;

    @BeforeEach
    void setUp() {
        this.mockUserQuizService = new UserQuizServiceImpl(
                this.mockTempQuizStorage,
                this.mockQuestionService,
                this.mockCategoryService,
                this.mockSolvedQuizRepository,
                this.mockUserService,
                this.mockUserStatsService,
                this.mockCategoryStatsService,
                this.mockQuestionStatsService);

        this.solvedQuiz = SolvedQuiz.builder()
                .id(1L)
                .categoryId(5L)
                .score(1)
                .maxScore(1)
                .questionIds(List.of(10L))
                .userAnswers(Map.of(10L, "A"))
                .solvedAt(LocalDateTime.now())
                .build();

        this.mockQuiz = Quiz.builder()
                .viewToken("token123")
                .categoryId(10L)
                .categoryName("Math")
                .expireAt(LocalDateTime.now().plusMinutes(30))
                .questions(List.of(
                        QuestionDTO.builder().id(1L).questionText("Q1").options(List.of("A","B")).correctAnswer("A").build(),
                        QuestionDTO.builder().id(2L).questionText("Q2").options(List.of("A","B")).correctAnswer("B").build()))
                .build();

        this.questionDTO = QuestionDTO.builder()
                .id(10L)
                .questionText("Test Question")
                .options(List.of("A", "B"))
                .correctAnswer("A")
                .build();
    }

    @Test
    void getSolvedQuizById_ShouldReturnError_WhenQuizNotFound() {
        when(this.mockSolvedQuizRepository.findById(5L))
                .thenReturn(Optional.empty());

        QuizNotFoundException exception = Assertions.assertThrows(QuizNotFoundException.class,
                () -> this.mockUserQuizService.getSolvedQuizById(5L));

        Assertions.assertEquals("Куизът не е намерен.", exception.getMessage());
    }

    @Test
    void getSolvedQuizById_ShouldReturnQuizDTO_WhenQuizExists() {
        when(this.mockSolvedQuizRepository.findById(1L)).thenReturn(Optional.of(solvedQuiz));
        when(this.mockQuestionService.getQuestionById(10L)).thenReturn(this.questionDTO);
        when(this.mockCategoryService.getCategoryNameById(5L)).thenReturn("Maths");

        QuizDTO result = this.mockUserQuizService.getSolvedQuizById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(solvedQuiz.getId(), result.getId());
        Assertions.assertEquals(solvedQuiz.getCategoryId(), result.getCategoryId());
        Assertions.assertEquals("Maths", result.getCategoryName());
        Assertions.assertEquals(solvedQuiz.getScore(), result.getCorrectAnswers());
        Assertions.assertEquals(solvedQuiz.getMaxScore(), result.getTotalQuestions());
        Assertions.assertEquals(solvedQuiz.getSolvedAt(), result.getSolvedAt());
        Assertions.assertEquals(solvedQuiz.getQuestionIds().size(), result.getQuestions().size());
        Assertions.assertEquals(solvedQuiz.getUserAnswers().size(), result.getUserAnswers().size());
    }

    @Test
    void getSolvedQuizzesByUsername_ShouldReturnPagedResult() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("solvedAt").descending());
        Page<SolvedQuiz> page = new PageImpl<>(List.of(this.solvedQuiz));

        when(this.mockSolvedQuizRepository
                .findByUserUsernameOrderBySolvedAtDesc("john", pageable))
                .thenReturn(page);
        when(this.mockCategoryService.getCategoryNameById(5L)).thenReturn("Maths");

        Page<QuizDTO> result = this.mockUserQuizService.getSolvedQuizzesByUsername("john", 0, 10);
        QuizDTO resultDTO = result.getContent().get(0);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertNotNull(resultDTO);
        Assertions.assertEquals(this.solvedQuiz.getId(), resultDTO.getId());
        Assertions.assertEquals(this.solvedQuiz.getCategoryId(), resultDTO.getCategoryId());
        Assertions.assertEquals("Maths", resultDTO.getCategoryName());
        Assertions.assertEquals(this.solvedQuiz.getScore(), resultDTO.getCorrectAnswers());
        Assertions.assertEquals(this.solvedQuiz.getMaxScore(), resultDTO.getTotalQuestions());
        Assertions.assertEquals(this.solvedQuiz.getSolvedAt(), resultDTO.getSolvedAt());
    }

    @Test
    void evaluateQuiz_ShouldReturnError_WhenUserNotFound() {
        QuizSubmissionDTO dto = new QuizSubmissionDTO();
        dto.setViewToken("token123");

        when(this.mockTempQuizStorage.get("token123")).thenReturn(this.mockQuiz);
        when(this.mockUserService.getUserByUsername("missing")).thenReturn(Optional.empty());

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
                () -> this.mockUserQuizService.evaluateQuiz(dto, "missing"));

        Assertions.assertEquals("Потребителят не е намерен.", exception.getMessage());
    }

    @Test
    void evaluateQuiz_ShouldEvaluateQuiz_WhenDataIsValid() {
        Map<Long, String> answers = Map.of(10L, "A");

        QuizSubmissionDTO dto = new QuizSubmissionDTO();
        dto.setViewToken("token123");
        dto.setAnswers(answers);

        User user = new User();
        user.setId(99L);
        user.setUserStatistics(new UserStatistics());

        when(this.mockTempQuizStorage.get("token123")).thenReturn(this.mockQuiz);
        when(this.mockUserService.getUserByUsername("john")).thenReturn(Optional.of(user));

        when(this.mockSolvedQuizRepository.saveAndFlush(any())).thenReturn(this.solvedQuiz);
        when(this.mockUserStatsService.updateUserStatistics(any(), anyLong(), anyInt(), any()))
                .thenReturn(new UserStatistics());

        Long result = this.mockUserQuizService.evaluateQuiz(dto, "john");

        Assertions.assertEquals(1L, result);
        verify(this.mockQuestionStatsService).updateOnQuizCompleted(eq(this.mockQuiz), eq(answers));
        verify(this.mockSolvedQuizRepository).saveAndFlush(any());
    }

    @Test
    void getQuizResult_ShouldReturnError_WhenQuizNotFound() {
        when(this.mockSolvedQuizRepository.findById(5L))
                .thenReturn(Optional.empty());

        QuizNotFoundException exception = Assertions.assertThrows(QuizNotFoundException.class,
                () -> this.mockUserQuizService.getQuizResult(5L));

        Assertions.assertEquals("Куизът не е намерен.", exception.getMessage());
    }

    @Test
    void getQuizResult_ShouldReturnQuizResultDTO_WhenQuizExists() {
        when(this.mockSolvedQuizRepository.findById(1L))
                .thenReturn(Optional.of(this.solvedQuiz));

        QuizResultDTO result = this.mockUserQuizService.getQuizResult(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(this.solvedQuiz.getId(), result.getId());
        Assertions.assertEquals(this.solvedQuiz.getScore(), result.getCorrectAnswers());
        Assertions.assertEquals(this.solvedQuiz.getMaxScore(), result.getTotalQuestions());
        double scorePercent = (double) this.solvedQuiz.getScore() / this.solvedQuiz.getMaxScore() * 100.00;
        Assertions.assertEquals(scorePercent, result.getScorePercent());
    }

    @Test
    void deleteQuizById_ShouldReturnFalse_WhenQuizNoneExist() {
        when(this.mockSolvedQuizRepository.existsById(5L)).thenReturn(false);

        boolean result = this.mockUserQuizService.deleteQuizById(5L);

        Assertions.assertFalse(result);
        verify(this.mockSolvedQuizRepository, times(1)).existsById(5L);
    }

    @Test
    void deleteQuizById_ShouldReturnTrue_WhenQuizExists() {
        when(this.mockSolvedQuizRepository.existsById(1L)).thenReturn(true);

        boolean result = this.mockUserQuizService.deleteQuizById(1L);

        Assertions.assertTrue(result);
        verify(this.mockSolvedQuizRepository, times(1)).existsById(1L);
        verify(this.mockSolvedQuizRepository, times(1)).deleteById(1L);
    }
}