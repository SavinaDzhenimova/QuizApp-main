package com.quizapp.repository;

import com.quizapp.model.entity.SolvedQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolvedQuizRepository extends JpaRepository<SolvedQuiz, Long> {

}