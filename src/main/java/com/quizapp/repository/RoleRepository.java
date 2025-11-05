package com.quizapp.repository;

import com.quizapp.model.entity.Role;
import com.quizapp.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

     Optional<Role> findByName(RoleName roleName);
}