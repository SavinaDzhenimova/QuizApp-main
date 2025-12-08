package com.quizapp.service;

import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.dto.user.AdminDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.model.entity.Role;
import com.quizapp.model.entity.User;
import com.quizapp.model.enums.RoleName;
import com.quizapp.repository.UserRepository;
import com.quizapp.service.events.AddedAdminEvent;
import com.quizapp.service.interfaces.PasswordResetService;
import com.quizapp.service.interfaces.RoleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RoleService mockRoleService;
    @Mock
    private PasswordResetService mockPasswordResetService;
    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @Mock
    private ApplicationEventPublisher mockAplEventPublisher;
    @InjectMocks
    private AdminServiceImpl mockAdminService;

    private AddAdminDTO mockAdmin;
    private Role mockRoleAdmin;
    private User testUser;

    @BeforeEach
    void setUp() {
        this.mockAdmin = new AddAdminDTO("admin", "admin@gmail.com");

        this.mockRoleAdmin = new Role();
        this.mockRoleAdmin.setName(RoleName.ADMIN);

        this.testUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@gmail.com")
                .password("encodedPass")
                .roles(new HashSet<>(Set.of(this.mockRoleAdmin)))
                .build();
    }

    @Test
    void getAllAdmins_ShouldReturnPageOfAdmins() {
        User testUser2 = User.builder().id(2L).username("admin2").email("admin2@gmail.com").build();

        Page<User> page = new PageImpl<>(List.of(this.testUser, testUser2));

        when(this.mockUserRepository.findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminDTO> result = this.mockAdminService.getAllAdmins("", pageable);

        Assertions.assertEquals(2, result.getContent().size());
        Assertions.assertEquals("admin", result.getContent().get(0).getUsername());
        Assertions.assertEquals("admin2", result.getContent().get(1).getUsername());
        Assertions.assertEquals("admin@gmail.com", result.getContent().get(0).getEmail());
        Assertions.assertEquals("admin2@gmail.com", result.getContent().get(1).getEmail());
    }

    @Test
    void addAdmin_ShouldReturnError_WhenDtoIsNull() {
        Result result = mockAdminService.addAdmin(null);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Не е намерен админ!", result.getMessage());
    }

    @Test
    void addAdmin_ShouldReturnError_WhenUsernameExists() {
        when(this.mockUserRepository.existsByUsername("admin")).thenReturn(true);

        Result result = this.mockAdminService.addAdmin(this.mockAdmin);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Вече съществува потребител с това потребителско име!", result.getMessage());
    }

    @Test
    void addAdmin_ShouldReturnError_WhenEmailExists() {
        when(this.mockUserRepository.existsByUsername("admin")).thenReturn(false);
        when(this.mockUserRepository.existsByEmail("admin@gmail.com")).thenReturn(true);

        Result result = this.mockAdminService.addAdmin(this.mockAdmin);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Вече съществува потребител с този имейл!", result.getMessage());
    }

    @Test
    void addAdmin_ShouldReturnError_WhenUserRoleNotFound() {
        when(this.mockUserRepository.existsByUsername("admin")).thenReturn(false);
        when(this.mockUserRepository.existsByEmail("admin@gmail.com")).thenReturn(false);
        when(this.mockRoleService.getRoleByName(RoleName.ADMIN)).thenReturn(Optional.empty());

        Result result = this.mockAdminService.addAdmin(this.mockAdmin);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Ролята Admin не съществува!", result.getMessage());
    }

    @Test
    void addAdmin_ShouldCreateAdmin_WhenDataIsValid() {
        when(this.mockUserRepository.existsByUsername("admin")).thenReturn(false);
        when(this.mockUserRepository.existsByEmail("admin@gmail.com")).thenReturn(false);
        when(this.mockRoleService.getRoleByName(RoleName.ADMIN)).thenReturn(Optional.of(this.mockRoleAdmin));
        when(this.mockPasswordEncoder.encode(anyString())).thenReturn("encodedPass");

        when(this.mockUserRepository.saveAndFlush(any(User.class))).thenReturn(this.testUser);
        when(this.mockPasswordResetService.createTokenForUser(any())).thenReturn("token123");

        Result result = this.mockAdminService.addAdmin(this.mockAdmin);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно добавихте нов админ.", result.getMessage());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(this.mockUserRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        Assertions.assertEquals("admin", savedUser.getUsername());
        Assertions.assertEquals("admin@gmail.com", savedUser.getEmail());
        Assertions.assertEquals("encodedPass", savedUser.getPassword());
        Assertions.assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN));

        verify(mockPasswordResetService).createTokenForUser(savedUser);

        ArgumentCaptor<AddedAdminEvent> eventCaptor = ArgumentCaptor.forClass(AddedAdminEvent.class);
        verify(mockAplEventPublisher).publishEvent(eventCaptor.capture());
        AddedAdminEvent publishedEvent = eventCaptor.getValue();

        Assertions.assertEquals(savedUser.getUsername(), publishedEvent.getUsername());
        Assertions.assertEquals("token123", publishedEvent.getToken());

        verifyNoMoreInteractions(mockUserRepository, mockPasswordResetService, mockAplEventPublisher);
    }

    @Test
    void deleteAdminById_ShouldReturnError_WhenUserNotFound() {
        when(this.mockUserRepository.findById(2L)).thenReturn(Optional.empty());

        Result result = this.mockAdminService.deleteAdminById(2L);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Не е намерен админ.", result.getMessage());
    }

    @Test
    void deleteAdminById_ShouldDeleteAdmin_WhenExists() {
        when(this.mockUserRepository.findById(1L)).thenReturn(Optional.of(this.testUser));

        Result result = this.mockAdminService.deleteAdminById(1L);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Успешно премахнахте админ admin.", result.getMessage());

        Assertions.assertTrue(this.testUser.getRoles().isEmpty());
        verify(this.mockUserRepository).saveAndFlush(testUser);
        verify(this.mockUserRepository).deleteById(1L);
    }
}