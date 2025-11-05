package com.quizapp.service;

import com.quizapp.model.entity.Role;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.RoleRepository;
import com.quizapp.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Optional<Role> getRoleByName(RoleName roleName) {
        return this.roleRepository.findByName(roleName);
    }
}