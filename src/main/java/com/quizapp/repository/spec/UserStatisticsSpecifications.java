package com.quizapp.repository.spec;

import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.entity.UserStatistics;
import com.quizapp.model.enums.RoleName;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserStatisticsSpecifications {

    public static Specification<UserStatistics> hasUsername(String username) {
        return (root, query, cb) -> {
            if (username == null || username.isBlank()) {
                return cb.conjunction();
            }

            Join<UserStatistics, User> userJoin = root.join("user");
            return cb.equal(userJoin.get("username"), username);
        };
    }

    public static Specification<UserStatistics> onlyRegularUsers() {
        return (root, query, cb) -> {
            Join<UserStatistics, User> userJoin = root.join("user");
            Join<User, Role> roleJoin = userJoin.join("roles");

            return cb.equal(roleJoin.get("name"), RoleName.USER);
        };
    }

    public static Specification<UserStatistics> sortByLastSolvedAtNullLast() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(cb.isNull(root.get("lastSolvedAt"))),
                    cb.desc(root.get("lastSolvedAt")));

            return null;
        };
    }
}