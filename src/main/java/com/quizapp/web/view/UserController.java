package com.quizapp.web.view;

import com.quizapp.model.dto.SolvedQuizDTO;
import com.quizapp.model.dto.user.UserDTO;
import com.quizapp.model.dto.user.UserRegisterDTO;
import com.quizapp.model.entity.Result;
import com.quizapp.service.interfaces.SolvedQuizService;
import com.quizapp.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final SolvedQuizService solvedQuizService;

    @GetMapping("/home")
    public ModelAndView showHomePage(@AuthenticationPrincipal UserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("home");

        UserDTO userDTO = this.userService.getUserInfo(userDetails.getUsername());

        modelAndView.addObject("user", userDTO);

        if (userDTO.getSolvedQuizzes().isEmpty()) {
            modelAndView.addObject("warning", "Все още нямате решени куизове.");
        }

        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @GetMapping("/login-error")
    public ModelAndView loginError(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Невалидно потребителско име или парола!");

        return new ModelAndView("redirect:/users/login");
    }

    @GetMapping("/register")
    public ModelAndView register(Model model) {
        if (!model.containsAttribute("userRegisterDTO")) {
            model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        }

        return new ModelAndView("register");
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO userRegisterDTO,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userRegisterDTO", userRegisterDTO)
                    .addFlashAttribute("org.springframework.validation.BindingResult.userRegisterDTO",
                            bindingResult);

            return new ModelAndView("register");
        }

        Result result = this.userService.registerUser(userRegisterDTO);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("failureMessage", result.getMessage());
            return new ModelAndView("redirect:/users/register");
        }

        return new ModelAndView("redirect:/users/login");
    }

    @GetMapping("/quizzes")
    public ModelAndView viewUserQuizzes(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(defaultValue = "0") int page) {

        int pageSize = 5;
        Page<SolvedQuizDTO> quizPage = this.solvedQuizService
                .getSolvedQuizzesByUsername(userDetails.getUsername(), page, pageSize);

        ModelAndView modelAndView = new ModelAndView("quizzes");

        if (quizPage.isEmpty()) {
            modelAndView.addObject("warning", "Все още нямате решени куизове.");
        } else {
            modelAndView.addObject("quizzes", quizPage.getContent());
            modelAndView.addObject("currentPage", page);
            modelAndView.addObject("totalPages", quizPage.getTotalPages());
        }

        return modelAndView;
    }
}