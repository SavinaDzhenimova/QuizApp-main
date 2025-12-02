package com.quizapp.repository.spec;

import com.quizapp.model.entity.CategoryStatistics;
import org.springframework.data.jpa.domain.Specification;

public class CategoryStatisticsSpecifications {

    public static Specification<CategoryStatistics> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null
                        ? null
                        : cb.equal(root.get("categoryId"), categoryId);
    }
}