package com.quizapp.repository;

import com.quizapp.model.entity.QuestionStatistics;
import com.quizapp.repository.spec.QuestionStatisticsSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class QuestionStatisticsRepositoryTest {

    @Autowired
    private QuestionStatisticsRepository questionStatisticsRepo;

    @BeforeEach
    void setUp() {
        QuestionStatistics questionStatistics = QuestionStatistics.builder()
                .questionId(1L)
                .questionText("Question")
                .categoryId(5L)
                .build();

        this.questionStatisticsRepo.save(questionStatistics);
    }

    @Test
    void findByQuestionId_ShouldReturnEmpty_WhenQuestionNotFound() {
        Optional<QuestionStatistics> optionalQuestionStatistics = this.questionStatisticsRepo.findByQuestionId(5L);

        assertThat(optionalQuestionStatistics).isEmpty();
    }

    @Test
    void findByQuestionId_ShouldReturnQuestionStatistics_WhenQuestionFound() {
        Optional<QuestionStatistics> optionalQuestionStatistics = this.questionStatisticsRepo.findByQuestionId(1L);

        assertThat(optionalQuestionStatistics).isPresent();
        assertThat(optionalQuestionStatistics.get().getQuestionId()).isEqualTo(1L);
    }

    @Test
    void findAllWithSpecification_ShouldReturnEmptyPage_WhenQuestionStatsNotFound() {
        Specification<QuestionStatistics> spec = Specification
                .allOf(QuestionStatisticsSpecifications.hasQuestionText("Question"))
                .and(QuestionStatisticsSpecifications.hasCategory(1L));
        Pageable pageable = PageRequest.of(0, 10);

        Page<QuestionStatistics> page = this.questionStatisticsRepo.findAll(spec, pageable);

        assertThat(page).isEmpty();
    }

    @Test
    void findAllWithSpecification_ShouldReturnPage_WhenQuestionStatsFound() {
        Specification<QuestionStatistics> spec = Specification
                .allOf(QuestionStatisticsSpecifications.hasQuestionText("Question"))
                .and(QuestionStatisticsSpecifications.hasCategory(5L));
        Pageable pageable = PageRequest.of(0, 10);

        Page<QuestionStatistics> page = this.questionStatisticsRepo.findAll(spec, pageable);

        assertThat(page).isNotEmpty();
        assertThat(page.getContent().get(0).getCategoryId()).isEqualTo(5L);
    }
}