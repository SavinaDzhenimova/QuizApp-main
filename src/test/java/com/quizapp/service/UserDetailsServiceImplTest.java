package com.quizapp.service;

import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository mockUserRepository;
    @InjectMocks
    private UserDetailsServiceImpl mockUserDetailsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName(RoleName.ADMIN);

        this.mockUser = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@gmail.com")
                .password("encodedPassword")
                .roles(Set.of(role))
                .solvedQuizzes(new ArrayList<>())
                .build();
    }

    @Test
    void loadUserByUsername_ShouldReturnError_WhenUsernameNotFound() {
        when(this.mockUserRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = Assertions.assertThrows(UsernameNotFoundException.class,
                () -> this.mockUserDetailsService.loadUserByUsername("missing"));

        Assertions.assertEquals("Username missing not found!", exception.getMessage());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUsernameFound() {
        when(this.mockUserRepository.findByUsername("user1"))
                .thenReturn(Optional.of(this.mockUser));

        UserDetails details = this.mockUserDetailsService.loadUserByUsername("user1");

        Assertions.assertNotNull(details);
        Assertions.assertEquals(this.mockUser.getUsername(), details.getUsername());
        Assertions.assertEquals(this.mockUser.getPassword(), details.getPassword());
        Assertions.assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        Assertions.assertTrue(details instanceof UserDetailsDTO);
        UserDetailsDTO dto = (UserDetailsDTO) details;
        Assertions.assertEquals(1L, dto.getId());
        Assertions.assertEquals("user1@gmail.com", dto.getEmail());
        Assertions.assertNotNull(dto.getSolvedQuizzes());
        Assertions.assertEquals(0, dto.getSolvedQuizzes().size());
    }

    @Test
    void loadUserByUsername_ShouldMapMultipleRoles() {
        Role roleUser = new Role();
        roleUser.setName(RoleName.USER);

        Role roleAdmin = new Role();
        roleAdmin.setName(RoleName.ADMIN);

        this.mockUser.setRoles(Set.of(roleUser, roleAdmin));

        when(this.mockUserRepository.findByUsername("user1"))
                .thenReturn(Optional.of(this.mockUser));

        UserDetails details = this.mockUserDetailsService.loadUserByUsername("user1");

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();

        Assertions.assertEquals(2, authorities.size());
        Assertions.assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        Assertions.assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}