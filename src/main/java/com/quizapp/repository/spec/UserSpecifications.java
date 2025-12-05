package com.quizapp.repository.spec;

import com.quizapp.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) ->
                username == null || username.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%");
    }
}