package com.quizapp.web;

import com.quizapp.config.SecurityConfig;
import com.quizapp.model.dto.AddInquiryDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.ContactsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContactsController.class)
@Import(SecurityConfig.class)
public class ContactsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactsService contactsService;

    @MockitoBean
    private GlobalController globalController;

    @Test
    void getContactsPage_ShouldReturnModelAndView() throws Exception {
        this.mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("addInquiryDTO"));
    }

    @Test
    void sendInquiry_ShouldReturnError_WhenBindingFails() throws Exception {
        this.mockMvc.perform(post("/contacts/send-inquiry")
                    .with(csrf())
                    .param("fullName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("addInquiryDTO"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.addInquiryDTO"));

        verify(this.contactsService, never()).sendInquiryEmail(any(AddInquiryDTO.class));
    }

    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @Test
    void sendInquiry_ShouldRedirectWithError_WhenDataIsInvalid() throws Exception {
        when(this.contactsService.sendInquiryEmail(any(AddInquiryDTO.class)))
                .thenReturn(new Result(false, "Невалидни входни данни."));

        this.mockMvc.perform(post("/contacts/send-inquiry")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("email", "john@gmail.com")
                        .param("theme", "Question about quizzes")
                        .param("message", "How to start a quiz?"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("error", "Невалидни входни данни."));

        verify(this.contactsService, times(1)).sendInquiryEmail(any(AddInquiryDTO.class));
    }

    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @Test
    void sendInquiry_ShouldRedirectWithSuccess_WhenDataIsValid() throws Exception {
        when(this.contactsService.sendInquiryEmail(any(AddInquiryDTO.class)))
                .thenReturn(new Result(true, "Вашето запитване беше изпратено успешно!"));

        this.mockMvc.perform(post("/contacts/send-inquiry")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("email", "john@gmail.com")
                        .param("theme", "Question about quizzes")
                        .param("message", "How to start a quiz?"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("success", "Вашето запитване беше изпратено успешно!"));

        verify(this.contactsService, times(1)).sendInquiryEmail(any(AddInquiryDTO.class));
    }

    @WithMockUser(username = "user", authorities = {"ROLE_ADMIN"})
    @Test
    void sendInquiry_ShouldNotAllowAdminToSendInquiry() throws Exception {
        this.mockMvc.perform(post("/contacts/send-inquiry")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("email", "john@gmail.com")
                        .param("theme", "Question about quizzes")
                        .param("message", "How to start a quiz?"))
                .andExpect(status().isForbidden());

        verify(this.contactsService, never()).sendInquiryEmail(any(AddInquiryDTO.class));
    }
}