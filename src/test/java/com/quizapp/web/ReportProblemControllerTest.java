package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.ReportProblemDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.ReportProblemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportProblemController.class)
@Import(SecurityConfig.class)
public class ReportProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportProblemService reportProblemService;

    @MockitoBean
    private GlobalController globalController;

    @WithAnonymousUser
    @Test
    void getReportProblemPage_ShouldReturnModelAndView_WhenAnonymous() throws Exception {
        this.mockMvc.perform(get("/report-problem"))
                .andExpect(status().isOk())
                .andExpect(view().name("report-problem"))
                .andExpect(model().attributeExists("reportProblemDTO"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void getReportProblemPage_ShouldReturnModelAndView_WhenUser() throws Exception {
        this.mockMvc.perform(get("/report-problem"))
                .andExpect(status().isOk())
                .andExpect(view().name("report-problem"))
                .andExpect(model().attributeExists("reportProblemDTO"));
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void getReportProblemPage_ShouldReturnError_WhenAdmin() throws Exception {
        this.mockMvc.perform(get("/report-problem"))
                .andExpect(status().isOk())
                .andExpect(view().name("report-problem"))
                .andExpect(model().attributeExists("reportProblemDTO"));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void reportProblem_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/report-problem/send-report")
                    .with(csrf())
                    .param("fullName", "")
                    .param("email", "john@gmail.com")
                    .param("problemType", "WRONG_ANSWER")
                    .param("questionIdentifier", "Identifier")
                    .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("report-problem"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.reportProblemDTO"));

        verify(this.reportProblemService, never()).sendEmailToReportProblem(any(ReportProblemDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void reportProblem_ShouldRedirectWithError_WhenDataNotValid() throws Exception {
        when(this.reportProblemService.sendEmailToReportProblem(any(ReportProblemDTO.class)))
                .thenReturn(new Result(false, "Невалидни входни данни."));

        this.mockMvc.perform(post("/report-problem/send-report")
                        .with(csrf())
                        .param("fullName", "JohnDoe")
                        .param("email", "john@gmail.com")
                        .param("problemType", "WRONG_ANSWER")
                        .param("questionIdentifier", "Question Identifier")
                        .param("description", "Problem description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/report-problem"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Невалидни входни данни."));

        verify(this.reportProblemService, times(1)).sendEmailToReportProblem(any(ReportProblemDTO.class));
    }

    @WithMockUser(authorities = {"ROLE_USER"})
    @Test
    void reportProblem_ShouldRedirectWithSuccess_WhenDataIsValid() throws Exception {
        when(this.reportProblemService.sendEmailToReportProblem(any(ReportProblemDTO.class)))
                .thenReturn(new Result(true, "Докладът Ви за проблем беше изпратен успешно!"));

        this.mockMvc.perform(post("/report-problem/send-report")
                        .with(csrf())
                        .param("fullName", "JohnDoe")
                        .param("email", "john@gmail.com")
                        .param("problemType", "WRONG_ANSWER")
                        .param("questionIdentifier", "Question Identifier")
                        .param("description", "Problem description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/report-problem"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Докладът Ви за проблем беше изпратен успешно!"));

        verify(this.reportProblemService, times(1)).sendEmailToReportProblem(any(ReportProblemDTO.class));
    }
}