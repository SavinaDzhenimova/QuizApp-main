package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.exception.GlobalExceptionHandler;
import com.quizapp.model.dto.user.AddAdminDTO;
import com.quizapp.model.dto.user.AdminDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private GlobalController globalController;

    private UserDetailsDTO loggedUser;
    private AdminDTO adminDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        this.loggedUser = UserDetailsDTO.builder()
                .id(1L)
                .username("logged")
                .email("logged@gmail.com")
                .password("Password123")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        this.adminDTO = AdminDTO.builder()
                .username("admin")
                .email("admin@gmail.com")
                .build();

        this.pageable = PageRequest.of(0, 10);
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showAdminsPage_ShouldReturnError_WhenUserIsNotAdmin() throws Exception {
        this.mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());

        verify(this.adminService, never()).getAllAdmins(eq(null), any(Pageable.class));
    }

    @Test
    void showAdminsPage_ShouldReturnModelAndView_WithAdmins() throws Exception {
        Page<AdminDTO> page = new PageImpl<>(List.of(this.adminDTO), this.pageable, 1);

        when(this.adminService.getAllAdmins(eq(null), any(Pageable.class))).thenReturn(page);

        this.mockMvc.perform(get("/admin").with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("admins"))
                .andExpect(model().attribute("loggedUserId", 1L))
                .andExpect(model().attributeExists("admins"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L))
                .andExpect(model().attribute("size", 10));

        verify(this.adminService).getAllAdmins(eq(null), any(Pageable.class));
    }

    @Test
    void showAdminsPage_ShouldAddWarning_WhenEmpty() throws Exception {
        when(this.adminService.getAllAdmins(eq(null), any(Pageable.class))).thenReturn(Page.empty());

        this.mockMvc.perform(get("/admin").with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("admins"))
                .andExpect(model().attribute("loggedUserId", 1L))
                .andExpect(model().attributeExists("admins"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 0L))
                .andExpect(model().attribute("size", 0))
                .andExpect(model().attributeExists("warning"))
                .andExpect(model().attribute("warning", "Няма намерени администратори."));
    }

    @Test
    void showAddAdminPage_ShouldReturnModelAndView() throws Exception {
        this.mockMvc.perform(get("/admin/add-admin").with(user(this.loggedUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("add-admin"))
                .andExpect(model().attributeExists("addAdminDTO"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showAddAdminPage_ShouldReturnError_WhenUserIsNotAdmin() throws Exception {
        this.mockMvc.perform(get("/admin/add-admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void showAddAdminPage_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/admin/add-admin")
                        .with(csrf())
                        .with(user(this.loggedUser))
                        .param("username", "")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("add-admin"))
                .andExpect(model().attributeExists("addAdminDTO"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.addAdminDTO"));
    }

    @Test
    void showAddAdminPage_ShouldRedirectWithSuccess_WhenAdminCreated() throws Exception {
        when(this.adminService.addAdmin(any(AddAdminDTO.class)))
                .thenReturn(new Result(true, "Успешно добавихте нов админ."));

        this.mockMvc.perform(post("/admin/add-admin")
                    .with(csrf())
                    .with(user(this.loggedUser))
                    .param("username", "admin1")
                    .param("email", "admin1@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/add-admin"))
                .andExpect(flash().attribute("success", "Успешно добавихте нов админ."));

        verify(this.adminService).addAdmin(any(AddAdminDTO.class));
    }

    @Test
    void showAddAdminPage_ShouldRedirectWithError_WhenAdminNotCreated() throws Exception {
        when(this.adminService.addAdmin(any(AddAdminDTO.class)))
                .thenReturn(new Result(false, "Вече съществува потребител с това потребителско име!"));

        this.mockMvc.perform(post("/admin/add-admin")
                    .with(csrf())
                    .with(user(this.loggedUser))
                    .param("username", "logged")
                    .param("email", "admin@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/add-admin"))
                .andExpect(flash().attribute("error", "Вече съществува потребител с това потребителско име!"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void showAddAdminPagePost_ShouldReturnError_WhenUserIsNotAdmin() throws Exception {
        this.mockMvc.perform(post("/admin/add-admin")
                        .with(csrf())
                        .param("username", "admin1")
                        .param("email", "admin1@gmail.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAdmin_ShouldRedirectWithError_WhenAdminNotFound() throws Exception {
        when(this.adminService.deleteAdminById(anyLong()))
                .thenReturn(new Result(false, "Не е намерен админ."));

        this.mockMvc.perform(delete("/admin/delete-admin/2")
                        .with(csrf())
                        .with(user(this.loggedUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Не е намерен админ."));

        verify(this.adminService, times(1)).deleteAdminById(anyLong());
    }

    @Test
    void deleteAdmin_ShouldRedirectWithSuccess_WhenAdminFound() throws Exception {
        when(this.adminService.deleteAdminById(anyLong()))
                .thenReturn(new Result(true, "Успешно премахнахте админ admin1."));

        this.mockMvc.perform(delete("/admin/delete-admin/2")
                        .with(csrf())
                        .with(user(this.loggedUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Успешно премахнахте админ admin1."));

        verify(this.adminService, times(1)).deleteAdminById(anyLong());
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void deleteAdmin_ShouldReturnError_WhenUser() throws Exception {
        this.mockMvc.perform(delete("/admin/delete-admin/2")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(this.adminService, never()).deleteAdminById(anyLong());
    }

    @WithAnonymousUser
    @Test
    void deleteAdmin_ShouldRedirectToLoginPage_WhenAnonymous() throws Exception {
        this.mockMvc.perform(delete("/admin/delete-admin/2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/users/login"));

        verify(this.adminService, never()).deleteAdminById(anyLong());
    }
}