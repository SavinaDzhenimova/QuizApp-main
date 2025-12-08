package com.quizapp.service;

import com.quizapp.model.entity.Role;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.RoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository mockRoleRepository;

    @InjectMocks
    private RoleServiceImpl mockRoleService;

    @Test
    void getRoleByName_ShouldReturnEmpty_WhenRoleNameNotFound() {
        when(this.mockRoleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

        Optional<Role> optionalRole = this.mockRoleService.getRoleByName(RoleName.USER);

        Assertions.assertTrue(optionalRole.isEmpty());
    }

    @Test
    void getRoleByName_ShouldReturnRole_WhenRoleNameExists() {
        Role role = new Role();
        role.setName(RoleName.USER);

        when(this.mockRoleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(role));

        Optional<Role> optionalRole = this.mockRoleService.getRoleByName(RoleName.USER);

        Assertions.assertFalse(optionalRole.isEmpty());
        Assertions.assertEquals(RoleName.USER, optionalRole.get().getName());
    }
}