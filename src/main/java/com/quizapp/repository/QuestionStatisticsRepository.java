package com.quizapp.repository;

import com.quizapp.model.entity.QuestionStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionStatisticsRepository extends JpaRepository<QuestionStatistics, Long> {

    Optional<QuestionStatistics> findByQuestionId(Long questionId);
}