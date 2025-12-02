package com.quizapp.repository;

import com.quizapp.model.entity.CategoryStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryStatisticsRepository extends JpaRepository<CategoryStatistics, Long> {

    Optional<CategoryStatistics> findByCategoryId(Long categoryId);

    Page<CategoryStatistics> findAll(Specification<CategoryStatistics> spec, Pageable pageable);
}