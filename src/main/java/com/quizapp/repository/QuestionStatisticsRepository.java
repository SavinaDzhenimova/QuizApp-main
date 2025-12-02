package com.quizapp.repository;

import com.quizapp.model.entity.QuestionStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionStatisticsRepository extends JpaRepository<QuestionStatistics, Long> {

    Optional<QuestionStatistics> findByQuestionId(Long questionId);

    Page<QuestionStatistics> findAll(Specification<QuestionStatistics> spec, Pageable pageable);
}