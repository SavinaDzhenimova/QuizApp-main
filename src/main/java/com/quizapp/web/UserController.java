package com.quizapp.web;

import com.quizapp.model.dto.quiz.QuizDTO;
import com.quizapp.model.dto.user.UpdatePasswordDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserDetailsDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.UserQuizService;
import com.quizapp.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserQuizService userQuizService;

    @GetMapping("/home")
    public ModelAndView showHomePage(@AuthenticationPrincipal UserDetailsDTO userDetailsDTO) {
        ModelAndView modelAndView = new ModelAndView("home");

        UserDTO userDTO = this.userService.getUserInfo(userDetailsDTO.getUsername());

        modelAndView.addObject("user", userDTO);

        boolean isUser = userDetailsDTO.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isUser) {
            modelAndView.addObject("userStats", userDTO.getUserStats());

            if (userDTO.getSolvedQuizzes().isEmpty()) {
                modelAndView.addObject("warning", "Все още нямате решени куизове.");
            }
        }

        return modelAndView;
    }

    @GetMapping("/quizzes")
    public ModelAndView viewUserQuizzes(@AuthenticationPrincipal UserDetailsDTO userDetailsDTO,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {

        Page<QuizDTO> quizPage = this.userQuizService
                .getSolvedQuizzesByUsername(userDetailsDTO.getUsername(), page, size);

        ModelAndView modelAndView = new ModelAndView("quizzes");

        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPages", quizPage.getTotalPages());
        modelAndView.addObject("size", size);

        if (quizPage.isEmpty()) {
            modelAndView.addObject("warning", "Все още нямате решени куизове.");
        } else {
            modelAndView.addObject("quizzes", quizPage.getContent());
        }

        return modelAndView;
    }

    @GetMapping("/update-password")
    public ModelAndView showUpdatePasswordPage(Model model) {
        if (!model.containsAttribute("updatePasswordDTO")) {
            model.addAttribute("updatePasswordDTO", new UpdatePasswordDTO());
        }

        return new ModelAndView("update-password");
    }

    @PostMapping("/update-password")
    public ModelAndView updatePassword(@Valid @ModelAttribute("updatePasswordDTO") UpdatePasswordDTO updatePasswordDTO,
                                               BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                               @AuthenticationPrincipal UserDetailsDTO userDetailsDTO) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("updatePasswordDTO", updatePasswordDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.updatePasswordDTO",
                            bindingResult);

            return new ModelAndView("update-password");
        }

        Result result = this.userService.updatePassword(userDetailsDTO.getUsername(), updatePasswordDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }

        return new ModelAndView("redirect:/users/update-password");
    }
}