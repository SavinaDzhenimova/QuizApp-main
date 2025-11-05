package com.quizapp.service.interfaces;

import com.quizapp.model.entity.Role;
import com.quizapp.model.enums.RoleName;

import java.util.Optional;

public interface RoleService {
    Optional<Role> getRoleByName(RoleName roleName);
}
