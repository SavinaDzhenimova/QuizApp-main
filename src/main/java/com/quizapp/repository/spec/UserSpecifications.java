package com.quizapp.repository.spec;

import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.RoleName;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> onlyAdminUsers() {
        return (root, query, cb) -> {
            Join<User, Role> roles = root.join("roles");

            return cb.equal(roles.get("name"), RoleName.ADMIN);
        };
    }

    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) ->
                username == null || username.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%");
    }
}