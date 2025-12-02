package com.quizapp.repository.spec;

import com.quizapp.model.entity.UserStatistics;
import org.springframework.data.jpa.domain.Specification;

public class UserStatisticsSpecifications {

    public static Specification<UserStatistics> hasUsername(String username) {
        return (root, query, cb) ->
                username == null || username.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%");
    }
}